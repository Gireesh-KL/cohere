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

    public void handleMessage(String prompt, String channel, List<Map<String, Object>> files) {
        StringBuilder context = new StringBuilder();
        String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();

        String userId = "unknown";
        if (files != null && !files.isEmpty() && files.get(0).get("user") != null) {
            userId = files.get(0).get("user").toString();
        }

        String sessionId = sessionStorageService.getSessionId(userId, channel);

        try {
            File folder = sessionStorageService.getSessionFolder(sessionId);
            File[] historyFiles = folder.listFiles((dir, name) -> name.startsWith("prompt_") || name.startsWith("response_"));
            if (historyFiles != null) {
                Arrays.sort(historyFiles, Comparator.comparingLong(File::lastModified)); // Oldest to newest
                for (int i = Math.max(0, historyFiles.length - 5); i < historyFiles.length; i++) {
                    context.append(Files.readString(historyFiles[i].toPath())).append("\n");
                }
            }
            sessionStorageService.saveText(sessionId, "prompt_" + System.currentTimeMillis() + ".txt", cleanedPrompt);
            System.out.println("Saved prompt: " + "prompt_" + System.currentTimeMillis() + ".txt");
        } catch (IOException e) {
            System.err.println("Failed to save prompt: " + e.getMessage());
        }

        if (files != null) {
            for (Map<String, Object> file : files) {
                try {
                    String url = (String) file.get("url_private");
                    String mimeType = inferMimeType((String) file.get("name"), (String) file.get("mimetype"));
                    byte[] fileBytes = slackClient.downloadFileAsBytes(url);

                    sessionStorageService.saveFile(sessionId, (String) file.get("name"), fileBytes);

                    System.out.println("Session [" + sessionId + "] updated. Files in session folder:");
                    File folder = sessionStorageService.getSessionFolder(sessionId);
                    for (File f : folder.listFiles()) {
                        System.out.println(" - " + f.getName());
                    }

                    String extracted = processingContext.process(fileBytes, mimeType, (String) file.get("name"));
                    context.append("\n").append(extracted);

                } catch (Exception e) {
                    context.append("\n[Error reading file: ").append(file.get("name")).append("]");
                }
            }
        }

        try {
            String response = cohereClient.callCohere(config.getCohereApiKey(), context + "\n\n" + cleanedPrompt);
            sessionStorageService.saveText(sessionId, "response_" + System.currentTimeMillis() + ".txt", response);
            System.out.println("Saved response: " + "response_" + System.currentTimeMillis() + ".txt");
            slackClient.sendMessage(channel, response);
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
