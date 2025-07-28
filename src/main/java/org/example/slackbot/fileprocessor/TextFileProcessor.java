package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length >= 10 && parts[7].trim().equals("ERROR")) {
                    String formatted = String.format(
                            "Timestamp: %s | Host: %s | Thread: %s | Level: %s | Class: %s | Method: %s | Message: %s\n",
                            parts[0].trim(),     // Timestamp
                            parts[1].trim(),     // Host
                            parts[4].trim(),     // Thread
                            parts[7].trim(),     // Level
                            parts[8].trim().replace("[", "").replace("]", ""), // Class
                            parts[9].trim(),     // Method
                            String.join("|", Arrays.copyOfRange(parts, 10, parts.length)).trim() // Message
                    );

                    allErrors.append(formatted);
                }
            }

        } catch (Exception e) {
            return "[Error extracting error logs: " + e.getMessage() + "]";
        }

        return allErrors.isEmpty() ? "[No error logs found.]" : allErrors.toString();
    }
}
