package org.example.slackbot.controller;

import org.example.slackbot.constants.SlackConstants;
import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.service.SlackBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slack")
public class SlackEventController {

    private final SlackBotService botService;

    public SlackEventController(SlackBotService botService) {
        this.botService = botService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> receiveEvent(@RequestBody SlackEvent slackEvent) {
        if (SlackConstants.EVENT_URL_VERIFICATION.equals(slackEvent.getType())) {
            return ResponseEntity.ok(slackEvent.getChallenge());
        }

        SlackEvent.InnerEvent event = slackEvent.getEvent();
        if (event == null || event.isFromBot()) {
            return ResponseEntity.ok().build();
        }

        if (SlackConstants.EVENT_APP_MENTION.equals(event.getType())) {
            new Thread(() -> botService.handleMessage(event.getText(), event.getChannel())).start();
        }

        return ResponseEntity.ok().build();
    }
}
