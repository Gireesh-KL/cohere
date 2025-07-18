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

import java.util.Map;

@RestController
public class SlackController {

    @Autowired
    private CohereService cohereService;

    @Autowired
    private AppConfig config;

    @PostMapping("/slack/events")
    public ResponseEntity<?> handleSlackEvent(@RequestBody SlackEvent slackEvent) throws Exception {
        System.out.println("SlackEvent Object: " + slackEvent);
        System.out.println("SlackEvent JSON: " + new ObjectMapper().writeValueAsString(slackEvent));

        if ("url_verification".equals(slackEvent.getType())) {
            return ResponseEntity.ok(slackEvent.getChallenge());
        }

        SlackEvent.InnerEvent event = slackEvent.getEvent();
        System.out.println("SlackEvent InnerEvent: " + event);
        System.out.println(event.getText());
        System.out.println(event.getUser());
        System.out.println(event.getChannel());
        System.out.println(event.getType());
        if (event != null && event.getText() != null && event.getUser() != null) {
            String response = cohereService.generateReply(event.getText());
            System.out.println(response);
            System.out.println("Cohere Response: " + response);
            sendMessageToSlack(event.getChannel(), response);
        }

        return ResponseEntity.ok().build();
    }

    private void sendMessageToSlack(String channel, String text) throws Exception {
        String jsonPayload = """
        {
          "channel": "%s",
          "text": "%s"
        }
        """.formatted(channel, text);

        System.out.println(jsonPayload);

        Request.post("https://slack.com/api/chat.postMessage")
                .addHeader("Authorization", "Bearer " + config.getSlackBotToken())
                .addHeader("Content-Type", "application/json")
                .bodyString(jsonPayload, ContentType.APPLICATION_JSON)
                .execute()
                .discardContent();
    }
}
