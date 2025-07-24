package org.example.slackbot.service;

import org.example.slackbot.client.CohereClient;
import org.example.slackbot.channel.SlackChannelClient;
import org.example.slackbot.config.SlackBotConfig;
import org.springframework.stereotype.Service;

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

    public void handleMessage(String prompt, String channel) {
        try {
            String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();
            String response = cohereClient.callCohere(config.getCohereApiKey(), cleanedPrompt);
            slackClient.sendMessage(channel, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
