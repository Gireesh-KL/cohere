package org.example.slackbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;

@Component
public class SessionCleaner {

    private static final String BASE_DIR = "session/";
    private static final long EXPIRATION_MS = 24 * 7 * 60 * 60 * 1000;

    @Scheduled(fixedRate = 604800000)
    public void cleanOldSessions() {
        File baseDir = new File(BASE_DIR);
        if (!baseDir.exists()) return;

        File[] sessions = baseDir.listFiles();
        if (sessions == null) return;

        for (File session : sessions) {
            if (session.isDirectory()) {
                long lastModified = session.lastModified();
                if (Instant.now().toEpochMilli() - lastModified > EXPIRATION_MS) {
                    deleteDirectory(session);
                    System.out.println("Deleted old session: " + session.getName());
                }
            }
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        dir.delete();
    }
}
