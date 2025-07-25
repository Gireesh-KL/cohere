package org.example.slackbot.fileprocessor;

public interface FileProcessor {
    boolean supports(String mimeType);
    String extractText(byte[] content, String fileName) throws Exception;
}
