package org.example.slackbot.fileprocessor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;

@Component
public class PdfFileProcessor implements FileProcessor {
    public boolean supports(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    public String extractText(byte[] content, String fileName) throws Exception {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(content))) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
