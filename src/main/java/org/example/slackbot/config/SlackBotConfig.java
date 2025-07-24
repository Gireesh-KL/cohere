package org.example.slackbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackBotConfig {
    @Value("${cohere.api.key}")
    private String cohereApiKey;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    @Value("${slack.bot.user-id}")
    private String slackBotUserId;

    public String getCohereApiKey() {
        return cohereApiKey;
    }

    public String getSlackBotToken() {
        return slackBotToken;
    }

    public String getSlackBotUserId() {
        return slackBotUserId;
    }
}
