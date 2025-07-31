package org.example.slackbot.service;

import org.example.slackbot.client.CohereClient;
import org.example.slackbot.channel.SlackChannelClient;
import org.example.slackbot.common.exception.SlackProcessingException;
import org.example.slackbot.config.SlackBotConfig;
import org.example.slackbot.model.SlackEvent;
import org.example.slackbot.process.FileProcessingContext;
import org.springframework.stereotype.Service;
import org.example.slackbot.fileprocessor.FileProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SlackBotService {

    private final SlackChannelClient slackClient;
    private final CohereClient cohereClient;
    private final SlackBotConfig config;
    private final FileProcessingContext processingContext;
    private final SessionStorageService sessionStorageService;

    public SlackBotService(SlackChannelClient slackClient,
                           CohereClient cohereClient,
                           SlackBotConfig config,
                           FileProcessingContext processingContext,
                           SessionStorageService sessionStorageService) {
        this.slackClient = slackClient;
        this.cohereClient = cohereClient;
        this.config = config;
        this.processingContext = processingContext;
        this.sessionStorageService = sessionStorageService;
    }

    public void handleMessage(String userId, String prompt, String channel, List<Map<String, Object>> files, String threadTs) {
        StringBuilder context = new StringBuilder();
        String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();

        String sessionId = sessionStorageService.getSessionId(userId, channel);
        StringBuilder contextBuilder = new StringBuilder();

        try {
            contextBuilder.append(sessionStorageService.getFullPromptHistory(sessionId));
            contextBuilder.append(sessionStorageService.getFullResponseHistory(sessionId));
        } catch (IOException e) {
            System.err.println("Failed to load session history: " + e.getMessage());
        }

        try {
            sessionStorageService.appendPrompt(sessionId, cleanedPrompt, null);
            System.out.println("Appended prompt to session " + sessionId);
        } catch (IOException e) {
            System.err.println("Failed to append prompt: " + e.getMessage());
        }

        if (files != null) {
            for (Map<String, Object> file : files) {
                try {
                    String fileName = (String) file.get("name");
                    String url = (String) file.get("url_private");
                    String mimeType = inferMimeType(fileName, (String) file.get("mimetype"));
                    byte[] fileBytes = slackClient.downloadFileAsBytes(url);

                    sessionStorageService.saveFile(sessionId, fileName, fileBytes);

                    System.out.println("Session [" + sessionId + "] updated. Files in session folder:");
                    File folder = sessionStorageService.getSessionFolder(sessionId);
                    for (File f : folder.listFiles()) {
                        System.out.println(" - " + f.getName());
                    }

                    String extracted = processingContext.process(fileBytes, mimeType, fileName);
                    context.append("\n").append(extracted);

                    sessionStorageService.appendPrompt(sessionId, extracted, fileName);
                    System.out.println("Processed and appended file: " + fileName);

                } catch (Exception e) {
                    context.append("\n[Error reading file: ").append(file.get("name")).append("]");
                }
            }
        }

        try {
            String response = cohereClient.callCohere(config.getCohereApiKey(), "Previous context was this: " + contextBuilder + "\nNow User Prompt is: " + context + "\n\n" + cleanedPrompt);
//            sessionStorageService.saveText(sessionId, "response_" + System.currentTimeMillis() + ".txt", response);
            sessionStorageService.appendResponse(sessionId, response, null);
            System.out.println("Response sent and saved");
            slackClient.sendMessage(channel, response, threadTs);
        } catch (Exception e) {
            throw new SlackProcessingException("Cohere call or Slack message failed", e);
        }
    }

    private String inferMimeType(String name, String mimeType) {
        if (mimeType == null || mimeType.equals("application/octet-stream")) {
            if (name.endsWith(".log")) return "text/plain";
            else if (name.endsWith(".csv")) return "text/csv";
            else if (name.endsWith(".pdf")) return "application/pdf";
        }
        return mimeType;
    }
}
