package org.example.slackbot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SlackBotConfig {
    @Value("${cohere.api.key}")
    private String cohereApiKey;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    @Value("${slack.bot.user-id}")
    private String slackBotUserId;

}
