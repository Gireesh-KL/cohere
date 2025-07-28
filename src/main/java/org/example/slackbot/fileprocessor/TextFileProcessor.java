package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
                return extractErrorChunks(fileBytes);
            }

            return content;
        } catch (Exception e) {
            return "[Error processing text file: " + e.getMessage() + "]";
        }
    }

    private String extractErrorChunks(byte[] fileBytes) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("| ERROR |")) {
                    if (currentChunk.length() + line.length() > MAX_CHUNK_SIZE) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                    }
                    currentChunk.append(line).append("\n");
                }
            }

            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

        } catch (Exception e) {
            return "[Error extracting logs: " + e.getMessage() + "]";
        }

        return chunks.isEmpty() ? "[No error logs found.]" : chunks.get(0);
    }
}
