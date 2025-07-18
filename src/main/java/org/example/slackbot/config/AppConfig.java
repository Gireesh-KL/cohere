package org.example.slackbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${cohere.api.key}")
    private String cohereApiKey;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    public String getCohereApiKey() {
        return cohereApiKey;
    }

    public String getSlackBotToken() {
        return slackBotToken;
    }
}

