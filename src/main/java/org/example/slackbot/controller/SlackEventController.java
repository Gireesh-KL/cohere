package org.example.slackbot.controller;

import org.example.slackbot.common.exception.SlackProcessingException;
import org.example.slackbot.constants.SlackConstants;
import org.example.slackbot.handler.SlackEventHandler;
import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.service.SlackBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

//@RestController
//@RequestMapping("/slack")
//public class SlackEventController {
//
//    private final SlackEventHandler handler;
//
//    public SlackEventController(SlackEventHandler handler) {
//        this.handler = handler;
//    }
////    private final SlackBotService botService;
////
////    public SlackEventController(SlackBotService botService) {
////        this.botService = botService;
////    }
//
//    @PostMapping("/events")
//    public ResponseEntity<?> receiveEvent(@RequestBody String requestBody) {
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            SlackEvent slackEvent = mapper.readValue(requestBody, SlackEvent.class);
//
//            if (SlackConstants.EVENT_URL_VERIFICATION.equals(slackEvent.getType())) {
//                return ResponseEntity.ok(slackEvent.getChallenge());
//            }
//
//            SlackEvent.InnerEvent event = slackEvent.getEvent();
//            if (event == null || event.isFromBot()) {
//                return ResponseEntity.ok().build();
//            }
//
//            if (SlackConstants.EVENT_APP_MENTION.equals(event.getType())) {
//                new Thread(() -> botService.handleMessage(event.getText(), event.getChannel(), event.getFiles())).start();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.ok().build();
//        }
//
//        return ResponseEntity.ok().build();
//    }
//}

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
