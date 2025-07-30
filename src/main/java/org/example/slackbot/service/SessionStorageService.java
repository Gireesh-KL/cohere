package org.example.slackbot.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class SessionStorageService {

    private static final String BASE_DIR = "session/";

    public String getSessionId(String userId, String channelId) {
        return userId + "_" + channelId;
    }

    public File getSessionFolder(String sessionId) {
        File folder = new File(BASE_DIR + sessionId);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public void saveText(String sessionId, String fileName, String content) throws IOException {
        File folder = getSessionFolder(sessionId);
        File file = new File(folder, fileName);
        Files.write(file.toPath(), content.getBytes());

    }

    public void saveFile(String sessionId, String fileName, byte[] content) throws IOException {
        File folder = getSessionFolder(sessionId);
        File file = new File(folder, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content);
        }
    }
}
