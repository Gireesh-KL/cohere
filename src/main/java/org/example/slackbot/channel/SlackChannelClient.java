package org.example.slackbot.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.example.slackbot.config.SlackBotConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlackChannelClient {

    private final SlackBotConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SlackChannelClient(SlackBotConfig config) {
        this.config = config;
    }

    public byte[] downloadFileAsBytes(String fileUrl) throws Exception {
        return Request.get(fileUrl)
                .addHeader("Authorization", "Bearer " + config.getSlackBotToken())
                .execute()
                .returnContent()
                .asBytes();
    }

    public void sendMessage(String channel, String text, String threadTs) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("text", text);
        if (threadTs != null && !threadTs.isEmpty()) {
            payload.put("thread_ts", threadTs);
        }

        String response = Request.post("https://slack.com/api/chat.postMessage")
                .addHeader("Authorization", "Bearer " + config.getSlackBotToken())
                .addHeader("Content-Type", "application/json")
                .bodyString(objectMapper.writeValueAsString(payload), ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

        System.out.println("Slack API Response: " + response);
    }
}
