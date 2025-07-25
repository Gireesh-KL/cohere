package org.example.slackbot.fileprocessor;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class TextFileProcessor implements FileProcessor {
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("text/");
    }

    public String extractText(byte[] content, String fileName) {
        return new String(content, StandardCharsets.UTF_8);
    }
}
