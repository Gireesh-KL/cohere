package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;

@Component
public class BinaryFileProcessor implements FileProcessor {
    public boolean supports(String mimeType) {
        return true; // fallback processor
    }

    public String extractText(byte[] content, String fileName) {
        return "[Binary content cannot be previewed]";
    }
}
