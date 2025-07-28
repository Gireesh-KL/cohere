package org.example.slackbot.process;

import org.example.slackbot.fileprocessor.FileProcessor;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingContext {

    private final FileProcessorFactory factory;

    public FileProcessingContext(FileProcessorFactory factory) {
        this.factory = factory;
    }

    public String process(byte[] content, String mimeType, String fileName) throws Exception {
        FileProcessor processor = factory.getProcessor(mimeType);
        return processor.extractText(content, fileName);
    }
}

