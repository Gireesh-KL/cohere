package org.example.slackbot.fileprocessor;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class TextFileProcessor implements FileProcessor {

    @Override
    public boolean supports(String mimeType) {
        return mimeType.startsWith("text/") || mimeType.equals("application/octet-stream");
    }

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        try {
            String content = new String(fileBytes, StandardCharsets.UTF_8);

            if (content.contains("\u0000")) {
                return "[Error: File appears to contain binary data and cannot be processed as text.]";
            }

            if (fileName.toLowerCase().endsWith(".log")) {
                return extractAllErrorLogs(fileBytes);
            }

            return content;
        } catch (Exception e) {
            return "[Error processing text file: " + e.getMessage() + "]";
        }
    }

    private String extractAllErrorLogs(byte[] fileBytes) {
        StringBuilder allErrors = new StringBuilder();
        Set<String> errorHashes = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {

            String line;
            int errorCount = 0;
            boolean insideErrorBlock = false;
            int stackTraceLines = 0;
            StringBuilder currentErrorBlock = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.contains("| ERROR |")) {
                    insideErrorBlock = true;
                    stackTraceLines = 0;
                    currentErrorBlock.append(line).append("\n");
                } else if (insideErrorBlock) {
                    if ((line.trim().startsWith("at") || !line.contains("|")) && stackTraceLines <= 10) {
                        currentErrorBlock.append(line.trim()).append("\n");
                        stackTraceLines++;
                    } else if (line.contains("|")) {
                        insideErrorBlock = false;

                        String hashContent = currentErrorBlock.toString()
                                .replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s\\|\\s", "");
                        String hash = DigestUtils.sha256Hex(hashContent);
                        System.out.println("Hashed Content: " + hashContent);
                        if (!errorHashes.contains(hash)) {
                            errorCount++;
                            allErrors.append("[ERROR ").append(errorCount).append("]\n")
                                    .append(currentErrorBlock).append("--------------\n");
                            errorHashes.add(hash);
                        }

                        currentErrorBlock.setLength(0);
                        if (line.contains("| ERROR |")) {
                            insideErrorBlock = true;
                            stackTraceLines = 0;
                            currentErrorBlock.append(line).append("\n");
                        }
                    }
                }
            }

            if (insideErrorBlock && !currentErrorBlock.isEmpty()) {
                String hashContent = currentErrorBlock.toString()
                        .replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s\\|\\s", "");
                String hash = DigestUtils.sha256Hex(hashContent);
                if (!errorHashes.contains(hash)) {
                    errorCount++;
                    allErrors.append("[ERROR ").append(errorCount).append("]\n")
                            .append(currentErrorBlock).append("--------------\n");
                    errorHashes.add(hash);
                }
            }

        } catch (Exception e) {
            return "[Error extracting error logs: " + e.getMessage() + "]";
        }

        System.out.println("All error list: " + allErrors);
        return allErrors.isEmpty() ? "[No error logs found.]" : allErrors.toString();
    }

}
