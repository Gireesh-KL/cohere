package org.example.slackbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${slack.bot.token}")
    public String slackBotToken;

    @Value("${cohere.api.key}")
    public String cohereApiKey;
}
