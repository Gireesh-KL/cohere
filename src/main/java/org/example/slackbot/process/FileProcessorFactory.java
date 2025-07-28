package org.example.slackbot.process;

import org.example.slackbot.common.exception.FileProcessingException;
import org.example.slackbot.fileprocessor.FileProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileProcessorFactory {
    private final List<FileProcessor> processors;

    public FileProcessorFactory(List<FileProcessor> processors) {
        this.processors = processors;
    }

    public FileProcessor getProcessor(String mimeType) {
        return processors.stream()
                .filter(p -> p.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new FileProcessingException("Unsupported file type: " + mimeType));
    }
}

