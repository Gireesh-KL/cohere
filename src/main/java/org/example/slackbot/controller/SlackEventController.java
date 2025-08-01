package org.example.slackbot.controller;

import org.example.slackbot.common.exception.SlackProcessingException;
import org.example.slackbot.constants.SlackConstants;
import org.example.slackbot.handler.SlackEventHandler;
import org.example.slackbot.model.SlackEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slack")
public class SlackEventController {

    private final SlackEventHandler handler;

    public SlackEventController(SlackEventHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/events")
    public ResponseEntity<?> receiveEvent(@RequestBody SlackEvent slackEvent) {
        try {
            if (SlackConstants.EVENT_URL_VERIFICATION.equals(slackEvent.getType())) {
                return ResponseEntity.ok(slackEvent.getChallenge());
            }

            if (slackEvent.getEvent() != null && !slackEvent.getEvent().isFromBot()) {
                if (SlackConstants.EVENT_APP_MENTION.equals(slackEvent.getEvent().getType())) {
                    handler.handleAppMention(slackEvent.getEvent());
                }
            }

        } catch (Exception e) {
            throw new SlackProcessingException("Failed to process Slack event", e);
        }

        return ResponseEntity.ok().build();
    }
}
