package org.example.slackbot.fileprocessor;

import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class CsvFileProcessor implements FileProcessor {

    @Value("${fileprocessor.csv.max-lines}")
    private int maxLines;
    @Override
    public boolean supports(String mimeType) {
        return mimeType.equals("text/csv");
    }

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord());

            int lineCount = 0;
            for (CSVRecord record : csvParser) {
                content.append(record.toString()).append("\n");
                lineCount++;
                if (lineCount >= maxLines) break;
            }
        } catch (Exception e) {
            return "[Error processing CSV: " + e.getMessage() + "]";
        }
        return content.toString();
    }
}
