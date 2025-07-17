package org.example.slackbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${cohere.api.key}")
    private String cohereApiKey;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    // getters
}

