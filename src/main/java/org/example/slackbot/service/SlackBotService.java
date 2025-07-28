package org.example.slackbot.service;

import org.example.slackbot.client.CohereClient;
import org.example.slackbot.channel.SlackChannelClient;
import org.example.slackbot.common.exception.SlackProcessingException;
import org.example.slackbot.config.SlackBotConfig;
import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.process.FileProcessingContext;
import org.springframework.stereotype.Service;
import org.example.slackbot.fileprocessor.FileProcessor;

import java.util.List;
import java.util.Map;

@Service
public class SlackBotService {

    private final SlackChannelClient slackClient;
    private final CohereClient cohereClient;
    private final SlackBotConfig config;
    private final FileProcessingContext processingContext;

    public SlackBotService(SlackChannelClient slackClient,
                           CohereClient cohereClient,
                           SlackBotConfig config,
                           FileProcessingContext processingContext) {
        this.slackClient = slackClient;
        this.cohereClient = cohereClient;
        this.config = config;
        this.processingContext = processingContext;
    }

    public void handleMessage(String prompt, String channel, List<Map<String, Object>> files) {
        StringBuilder context = new StringBuilder();
        String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();

        if (files != null) {
            for (Map<String, Object> file : files) {
                try {
                    String url = (String) file.get("url_private");
                    String mimeType = inferMimeType((String) file.get("name"), (String) file.get("mimetype"));
                    byte[] fileBytes = slackClient.downloadFileAsBytes(url);

                    String extracted = processingContext.process(fileBytes, mimeType, (String) file.get("name"));
                    context.append("\n").append(extracted);

                } catch (Exception e) {
                    context.append("\n[Error reading file: ").append(file.get("name")).append("]");
                }
            }
        }

        try {
            String response = cohereClient.callCohere(config.getCohereApiKey(), context + "\n\n" + cleanedPrompt);
            slackClient.sendMessage(channel, response);
        } catch (Exception e) {
            throw new SlackProcessingException("Cohere call or Slack message failed", e);
        }
    }

    private String inferMimeType(String name, String mimeType) {
        if (mimeType == null || mimeType.equals("application/octet-stream")) {
            if (name.endsWith(".log")) return "text/plain";
            else if (name.endsWith(".csv")) return "text/csv";
            else if (name.endsWith(".pdf")) return "application/pdf";
        }
        return mimeType;
    }
}

