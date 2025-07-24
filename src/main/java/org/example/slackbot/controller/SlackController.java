package org.example.slackbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.service.CohereService;
import org.example.slackbot.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.util.HashMap;
import java.util.Map;



@RestController
public class SlackController {

    @Autowired
    private CohereService cohereService;

    @Autowired
    private AppConfig config;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/slack/events")
    public ResponseEntity<?> handleSlackEvent(@RequestBody SlackEvent slackEvent) throws Exception {
        System.out.println("SlackEvent Object: " + slackEvent);
        System.out.println("Raw SlackEvent JSON: " + objectMapper.writeValueAsString(slackEvent));

        if ("url_verification".equals(slackEvent.getType())) {
            return ResponseEntity.ok(slackEvent.getChallenge());
        }

        SlackEvent.InnerEvent event = slackEvent.getEvent();

        if (event == null || event.isFromBot() || "bot_message".equals(event.getSubtype())) {
            System.out.println("Ignored bot/self event: " + event);
            return ResponseEntity.ok().build();
        }

        if (!"app_mention".equals(event.getType())) {
            System.out.println("Ignored: Not an app_mention event");
            return ResponseEntity.ok().build();
        }


        System.out.println("SlackEvent InnerEvent: " + event);
        System.out.println(event.getText());
        System.out.println(event.getUser());
        System.out.println(event.getChannel());
        System.out.println(event.getType());

        ResponseEntity<?> slackAck = ResponseEntity.ok().build();

        new Thread(() -> {
            try {
                if (event.getText() != null && event.getUser() != null) {
                    System.out.println("Processing user event asynchronously...");
                    String prompt = event.getText().replaceAll("<@\\w+>", "").trim();
                    String response = cohereService.generateReply(prompt);
                    System.out.println("Cohere Response: " + response);
                    sendMessageToSlack(event.getChannel(), response);
                }
                else {
                    System.out.println("Use Null or Text Null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return slackAck;
    }
//        if (event.getText() != null && event.getUser() != null) {
//            System.out.println("I'm inside cohereservice section");
//            String prompt = event.getText().replaceAll("<@\\w+>", "").trim();
//            String response = cohereService.generateReply(prompt);
//            System.out.println("Cohere Response: " + response);
//            sendMessageToSlack(event.getChannel(), response);
//        }

//        return ResponseEntity.ok().build();
//    }

    private void sendMessageToSlack(String channel, String text) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("text", text);
        String jsonPayload = objectMapper.writeValueAsString(payload);
        System.out.println("Final Payload: " + jsonPayload);

        String response = Request.post("https://slack.com/api/chat.postMessage")
                .addHeader("Authorization", "Bearer " + config.getSlackBotToken())
                .addHeader("Content-Type", "application/json")
                .bodyString(jsonPayload, ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

        System.out.println("Slack API Response: " + response);
    }
}
