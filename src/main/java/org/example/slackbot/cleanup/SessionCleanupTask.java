package org.example.slackbot.cleanup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;

@Component
public class SessionCleanupTask {

    private static final String BASE_DIR = "session/";
    private static final Duration EXPIRY = Duration.ofHours(4);

    @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
    public void cleanupOldFolders() {
        File base = new File(BASE_DIR);
        if (!base.exists()) return;

        for (File sessionFolder : base.listFiles()) {
            if (sessionFolder.isDirectory()) {
                long lastModified = sessionFolder.lastModified();
                long age = System.currentTimeMillis() - lastModified;
                if (age > EXPIRY.toMillis()) {
                    deleteDirectory(sessionFolder);
                }
            }
        }
    }

    private void deleteDirectory(File folder) {
        for (File file : folder.listFiles()) {
            file.delete();
        }
        folder.delete();
    }
}
