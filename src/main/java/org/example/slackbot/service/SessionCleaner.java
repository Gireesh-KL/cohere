package org.example.slackbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.time.Instant;
import java.util.Objects;

@Component
public class SessionCleaner {

    @Value("${session.base-dir}")
    private String BASE_DIR;

    @Value("${session.expiration-ms}") // default: 7 days
    private long EXPIRATION_MS;

    @Scheduled(fixedRateString = "${session.cleanup-rate-ms}")
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
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                deleteDirectory(file);
            }
        }
        boolean flag = dir.delete();
        if(!flag){
            System.out.println("Failed to delete directory: " + dir.getAbsolutePath());
        }
    }
}
