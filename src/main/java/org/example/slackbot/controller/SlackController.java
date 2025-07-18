package org.example.slackbot.controller;

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
        if ("url_verification".equals(slackEvent.getType())) {
            String challenge = slackEvent.getType();
            return ResponseEntity.ok(challenge);
        }
        Map<String, Object> event = slackEvent.getEvent();
        String user = (String) event.get("user");
        String text = (String) event.get("text");
        String channel = (String) event.get("channel");

        if (text != null && user != null) {
            String response = cohereService.generateReply(text);
            System.out.println(response);
            sendMessageToSlack(channel, response);
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
