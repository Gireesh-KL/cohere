package org.example.slackbot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.springframework.stereotype.Component;

@Component
public class CohereClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${cohere.api.max_tokens}")
    private int maxTokens;

    @Value("${cohere.api.temperature}")
    private double temperature;

    @Value("${cohere.api.model}")
    private String model;
    public String callCohere(String apiKey, String userPrompt) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("prompt", userPrompt);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        String response = Request.post("https://api.cohere.ai/v1/generate")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .bodyString(requestBody.toString(), ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

        JsonNode textNode = objectMapper.readTree(response)
                .path("generations")
                .path(0)
                .path("text");

        if (textNode.isMissingNode()) {
            throw new RuntimeException("Cohere API did not return expected text.");
        }

        return textNode.asText().trim();
    }
}
