package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TextFileProcessor implements FileProcessor {

    private static final int MAX_CHUNK_SIZE = 4000;
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
                System.out.println("I entered here to extract logs: " + fileName);
                List<String> chunks = extractErrorChunks(fileBytes);
                int cnt = 0;
                for(String line : chunks) {
                    System.out.println("Line No" + cnt + ": " + line);
                    cnt++;
                }
                return chunks.isEmpty() ? "[No error logs found.]" : chunks.get(0);
            }

            return content;
        } catch (Exception e) {
            return "[Error processing text file: " + e.getMessage() + "]";
        }
    }

    private List<String> extractErrorChunks(byte[] fileBytes) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

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

                    if (currentChunk.length() + formatted.length() > MAX_CHUNK_SIZE) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }

                    currentChunk.append(formatted);
                }
            }

            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

        } catch (Exception e) {
            chunks.add("[Error extracting error logs: " + e.getMessage() + "]");
        }

        return chunks;
    }
}
