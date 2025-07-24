package org.example.slackbot.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CohereService {
    @Value("${cohere.api.key}")
    private String cohereApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateReply(String userPrompt) throws Exception {
//        System.out.println(cohereApiKey);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "command-r-plus");
        requestBody.put("prompt", userPrompt);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);
//        String jsonRequest = """
//        {
//          "model": "command-r-plus",
//          "prompt": "%s",
//          "max_tokens": 10000,
//          "temperature": 0.7
//        }
//        """.formatted(userPrompt.replace("\"", "\\\""));
        String jsonRequest = mapper.writeValueAsString(requestBody);

        String response = Request.post("https://api.cohere.ai/v1/generate")
                .addHeader("Authorization", "Bearer " + cohereApiKey)
                .addHeader("Content-Type", "application/json")
                .bodyString(jsonRequest, ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode textNode = jsonNode.path("generations").path(0).path("text");
        if (textNode.isMissingNode()) {
            throw new RuntimeException("Cohere API did not return expected text.");
        }
        return textNode.asText().trim();
//        String reply = response.split("\"text\":\"")[1].split("\"")[0];
//        return reply.replace("\\n", "\n").trim();
    }
}
