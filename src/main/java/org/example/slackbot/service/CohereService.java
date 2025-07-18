package org.example.slackbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

@Service
public class CohereService {
    @Value("${cohere.api.key}")
    private String cohereApiKey;

    public String generateReply(String userPrompt) throws Exception {
        System.out.println(cohereApiKey);
        String jsonRequest = """
        {
          "model": "command-r-plus",
          "prompt": "%s",
          "max_tokens": 10000,
          "temperature": 0.7
        }
        """.formatted(userPrompt);

        String response = Request.post("https://api.cohere.ai/v1/generate")
                .addHeader("Authorization", "Bearer " + cohereApiKey)
                .addHeader("Content-Type", "application/json")
                .bodyString(jsonRequest, ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

        // Extract text manually (can also use Jackson)
        String reply = response.split("\"text\":\"")[1].split("\"")[0];
        return reply.replace("\\n", "\n").trim();
    }
}
