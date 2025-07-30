package org.example.slackbot.handler;

import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.service.SlackBotService;
import org.springframework.stereotype.Service;

@Service
public class SlackEventHandler {

    private final SlackBotService slackBotService;

    public SlackEventHandler(SlackBotService slackBotService) {
        this.slackBotService = slackBotService;
    }

    public void handleAppMention(SlackEvent.InnerEvent event) {
        new Thread(() -> slackBotService.handleMessage(event.getUser(), event.getText(), event.getChannel(), event.getFiles())).start();
    }
}

