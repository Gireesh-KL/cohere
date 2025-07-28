package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class LogFileProcessor implements FileProcessor {
    @Override
    public boolean supports(String mimeType) {
        return mimeType.equals("text/plain") || mimeType.equals("application/octet-stream");
    }

    @Override
    public String extractText(byte[] content, String fileName) {
        return new String(content, StandardCharsets.UTF_8);
    }
}
