package org.example.slackbot.service;

import org.example.slackbot.client.CohereClient;
import org.example.slackbot.channel.SlackChannelClient;
import org.example.slackbot.config.SlackBotConfig;
import org.example.slackbot.model.SlackEvent;
import org.springframework.stereotype.Service;
import org.example.slackbot.fileprocessor.FileProcessor;

import java.util.List;
import java.util.Map;

@Service
public class SlackBotService {

    private final CohereClient cohereClient;
    private final SlackChannelClient slackClient;
    private final SlackBotConfig config;
    private final List<FileProcessor> fileProcessors;

    public SlackBotService(CohereClient cohereClient,
                           SlackChannelClient slackClient,
                           SlackBotConfig config,
                           List<FileProcessor> fileProcessors) {
        this.cohereClient = cohereClient;
        this.slackClient = slackClient;
        this.config = config;
        this.fileProcessors = fileProcessors;
    }

    public void handleMessage(String prompt, String channel, List<Map<String, Object>> files) {
        StringBuilder contextText = new StringBuilder();

        try {
            String cleanedPrompt = prompt.replaceAll("<@\\w+>", "").trim();

            if (files != null) {
                for (Map<String, Object> fileMap : files) {
                    try {
                        String url = (String) fileMap.get("url_private");
                        String mimeType = (String) fileMap.get("mimetype");
                        String name = (String) fileMap.get("name");

                        if (mimeType == null || mimeType.equals("application/octet-stream")) {
                            if (name.endsWith(".log")) mimeType = "text/plain";
                            else if (name.endsWith(".csv")) mimeType = "text/csv";
                            else if (name.endsWith(".pdf")) mimeType = "application/pdf";
                            else mimeType = "application/octet-stream";
                        }

                        System.out.println(url);
                        System.out.println(mimeType);
                        System.out.println(name);

                        byte[] fileBytes = slackClient.downloadFileAsBytes(url);

                        boolean processed = false;
                        for (FileProcessor processor : fileProcessors) {
                            if (processor.supports(mimeType)) {
                                String extracted = processor.extractText(fileBytes, name);
                                System.out.println("Extracted text" + extracted);
                                contextText.append("\n").append(extracted);
                                System.out.println("Context: " + contextText);
                                processed = true;
                                break;
                            }
                        }

                        if (!processed) {
                            contextText.append("\n[Unsupported file: ").append(name).append("]");
                        }

                    } catch (Exception e) {
                        contextText.append("\n[Error reading file: ").append(fileMap.get("name")).append("]");
                        e.printStackTrace();
                    }
                }
            }

            String fullPrompt = contextText + "\n\n" + cleanedPrompt;
            String response = cohereClient.callCohere(config.getCohereApiKey(), fullPrompt);
            slackClient.sendMessage(channel, response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                slackClient.sendMessage(channel, "An error occurred while processing your request.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
