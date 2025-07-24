package org.example.slackbot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.springframework.stereotype.Component;

@Component
public class CohereClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callCohere(String apiKey, String userPrompt) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "command-r-plus");
        requestBody.put("prompt", userPrompt);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);

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
