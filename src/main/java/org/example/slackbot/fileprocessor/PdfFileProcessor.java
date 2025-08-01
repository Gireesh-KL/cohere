package org.example.slackbot.fileprocessor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class PdfFileProcessor implements FileProcessor {
    @Override
    public boolean supports(String mimeType) {
        return mimeType.equals("application/pdf");
    }

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            return "[Error processing PDF: " + e.getMessage() + "]";
        }
    }
}
