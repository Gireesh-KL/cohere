package org.example.slackbot.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class SessionStorageService {

    private static final String BASE_DIR = "session/";

    public String getSessionId(String threadTs) {
        return threadTs;
    }

    public File getSessionFolder(String sessionId) {
        File folder = new File(BASE_DIR + sessionId);
        if (!folder.exists()) {
            boolean flag = folder.mkdirs();
            if(!flag){
                System.out.println("Failed to create directory: " + folder.getAbsolutePath());
            }
        }
        System.out.println(BASE_DIR + sessionId);
        return folder;
    }

    public void appendPrompt(String sessionId, String prompt, String sourceFileName) throws IOException {
        File folder = getSessionFolder(sessionId);
        File file = new File(folder, "prompts.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("[Prompt");
            if (sourceFileName != null) writer.write(" from: " + sourceFileName);
            writer.write("] " + prompt + "\n\n");
        }
    }

    public void appendResponse(String sessionId, String response, String sourceFileName) throws IOException {
        File folder = getSessionFolder(sessionId);
        File file = new File(folder, "responses.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("[Response");
            if (sourceFileName != null) writer.write(" for: " + sourceFileName);
            writer.write("] " + response + "\n\n");
        }
    }

    public String getFullPromptHistory(String sessionId) throws IOException {
        File folder = getSessionFolder(sessionId);
        File promptFile = new File(folder, "prompts.txt");
        if (!promptFile.exists()) return "";
        return Files.readString(promptFile.toPath());
    }

    public String getFullResponseHistory(String sessionId) throws IOException {
        File folder = getSessionFolder(sessionId);
        File responseFile = new File(folder, "responses.txt");
        if (!responseFile.exists()) return "";
        return Files.readString(responseFile.toPath());
    }

    public void saveFile(String sessionId, String fileName, byte[] content) throws IOException {
        File folder = getSessionFolder(sessionId);
        File file = new File(folder, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content);
        }
    }
}
