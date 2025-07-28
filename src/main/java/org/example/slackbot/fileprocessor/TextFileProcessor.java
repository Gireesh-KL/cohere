package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class TextFileProcessor implements FileProcessor {
    @Override
    public boolean supports(String mimeType) {
        return mimeType.startsWith("text/") || mimeType.equals("application/octet-stream");
    }

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        try {
            String text = new String(fileBytes, StandardCharsets.UTF_8);
            if (text.contains("\u0000")) throw new IllegalArgumentException("Binary content detected");
            return text;
        } catch (Exception e) {
            return "[Error processing text file or binary detected: " + e.getMessage() + "]";
        }
    }
}
