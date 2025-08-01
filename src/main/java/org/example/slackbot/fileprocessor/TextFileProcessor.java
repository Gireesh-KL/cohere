package org.example.slackbot.fileprocessor;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Arrays;
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

//    private String extractAllErrorLogs(byte[] fileBytes) {
//        StringBuilder allErrors = new StringBuilder();
//
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split("\\|");
//
//                if (parts.length >= 10 && parts[7].trim().equals("ERROR")) {
//                    String formatted = String.format(
//                            "Timestamp: %s | Host: %s | Thread: %s | Level: %s | Class: %s | Method: %s | Message: %s\n",
//                            parts[0].trim(),     // Timestamp
//                            parts[1].trim(),     // Host
//                            parts[4].trim(),     // Thread
//                            parts[7].trim(),     // Level
//                            parts[8].trim().replace("[", "").replace("]", ""), // Class
//                            parts[9].trim(),     // Method
//                            String.join("|", Arrays.copyOfRange(parts, 10, parts.length)).trim() // Message
//                    );
//
//                    allErrors.append(formatted);
//                }
//            }
//
//        } catch (Exception e) {
//            return "[Error extracting error logs: " + e.getMessage() + "]";
//        }
//
//        return allErrors.isEmpty() ? "[No error logs found.]" : allErrors.toString();
//    }

//    private String extractAllErrorLogs(byte[] fileBytes) {
//        StringBuilder allErrors = new StringBuilder();
//        Set<String> errorHashes = new HashSet<>();
//
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {
//
//            String line;
//            int errorCount = 0;
//
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split("\\|");
//
//                boolean errorblock = true;
//                if (parts.length >= 10 && parts[7].trim().equals("ERROR")) {
//                    StringBuilder errorBlock = new StringBuilder();
//
//                    String timestamp = parts[0].trim();
//                    String host = parts[1].trim();
//                    String thread = parts[4].trim();
//                    String level = parts[7].trim();
//                    String clazz = parts[8].trim().replace("[", "").replace("]", "");
//                    String method = parts[9].trim();
//                    String message = String.join("|", Arrays.copyOfRange(parts, 10, parts.length)).trim();
//
//                    errorBlock.append("Timestamp: ").append(timestamp).append(" | Host: ").append(host)
//                            .append(" | Thread: ").append(thread).append(" | Level: ").append(level)
//                            .append(" | Class: ").append(clazz).append(" | Method: ").append(method)
//                            .append(" | Message: ").append(message).append("\n");
//
//                    int stackLineCount = 0;
//                    reader.mark(5000);
//                    while ((line = reader.readLine()) != null) {
//                        if (line.trim().startsWith("at")) {
//                            errorBlock.append("Stack Trace: ").append(line.trim()).append("\n");
//                            stackLineCount++;
//                            if (stackLineCount >= 10) break;
//                        } else if (!line.contains("|")) {
//                            errorBlock.append("Stack Trace: ").append(line.trim()).append("\n");
//                            stackLineCount++;
//                            if (stackLineCount >= 10) break;
//                        } else {
//                            reader.reset();
//                            break;
//                        }
//                        reader.mark(5000);
//                    }
//
//                    String contentToHash = errorBlock.toString().replaceFirst("Timestamp: .*?\\|", "");
//                    String hash = DigestUtils.sha256Hex(contentToHash);
//
//                    if (!errorHashes.contains(hash)) {
//                        errorCount++;
//                        allErrors.append("[ERROR ").append(errorCount).append("]\n")
//                                .append(errorBlock).append("--------------\n");
//                        errorHashes.add(hash);
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            return "[Error extracting error logs: " + e.getMessage() + "]";
//        }
//        System.out.println(allErrors.toString());
//        return allErrors.isEmpty() ? "[No error logs found.]" : allErrors.toString();
//    }

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
                    if ((line.trim().startsWith("at") || !line.contains("|")) && stackTraceLines < 10) {
                        currentErrorBlock.append(line.trim()).append("\n");
                        stackTraceLines++;
                    } else if (line.contains("|")) {
                        insideErrorBlock = false;

                        String hashContent = currentErrorBlock.toString().replaceFirst("Timestamp: .*?\\|", "");
                        String hash = DigestUtils.sha256Hex(hashContent);

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

            if (insideErrorBlock && currentErrorBlock.length() > 0) {
                String hashContent = currentErrorBlock.toString().replaceAll("Timestamp: .*?\\|", "");
//                System.out.println("Hashed Content: " + hashContent);
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

        System.out.println("All error list: " + allErrors.toString());
        return allErrors.isEmpty() ? "[No error logs found.]" : allErrors.toString();
    }

}
