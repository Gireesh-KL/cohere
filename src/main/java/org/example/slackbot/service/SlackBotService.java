package org.example.slackbot.service;

import org.example.slackbot.client.CohereClient;
import org.example.slackbot.channel.SlackChannelClient;
import org.example.slackbot.config.SlackBotConfig;
import org.example.slackbot.model.SlackEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SlackBotService {

    private final CohereClient cohereClient;
    private final SlackChannelClient slackClient;
    private final SlackBotConfig config;

    public SlackBotService(CohereClient cohereClient, SlackChannelClient slackClient, SlackBotConfig config) {
        this.cohereClient = cohereClient;
        this.slackClient = slackClient;
        this.config = config;
    }

    public void handleMessage(String prompt, String channel, List<Map<String, Object>> files) {
        try {
            String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();

            StringBuilder contextText = new StringBuilder();
            if (files != null && !files.isEmpty()) {
                for (Map<String, Object> fileMap : files) {
                    String url = (String) fileMap.get("url_private");
                    if (url != null) {
                        String content = slackClient.downloadFile(url);
                        contextText.append("\n").append(content);
                    }
                }
                System.out.println("File was like this" + contextText);
            }

            String fullPrompt = contextText + "\n\n" + cleanedPrompt;

            String response = cohereClient.callCohere(config.getCohereApiKey(), fullPrompt);
            slackClient.sendMessage(channel, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
