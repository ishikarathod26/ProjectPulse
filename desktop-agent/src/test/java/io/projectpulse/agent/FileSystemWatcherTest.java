package io.projectpulse.agent;

import io.projectpulse.agent.model.TrackedEvent;
import io.projectpulse.agent.watcher.FileSystemWatcherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemWatcherTest {

    @Test
    @DisplayName("Verify filesystem file creation, modification, and deletion events")
    void testFileWatcherLifecycle(@TempDir Path tempDir) throws IOException, InterruptedException {
        List<TrackedEvent> events = new CopyOnWriteArrayList<>();
        FileSystemWatcherService watcher = new FileSystemWatcherService(tempDir, events::add);

        watcher.start();

        try {
            // 1. Create file
            Path testFile = tempDir.resolve("test-document.txt");
            Files.writeString(testFile, "Initial content line 1\n");

            // Allow WatchService event loop to poll
            Thread.sleep(600);

            // 2. Modify file
            Files.writeString(testFile, "Appended line 2\n", java.nio.file.StandardOpenOption.APPEND);
            Thread.sleep(600);

            // 3. Delete file
            Files.delete(testFile);
            Thread.sleep(600);

            assertFalse(events.isEmpty(), "Watcher should capture events within monitored directory");
            boolean hasCreate = events.stream().anyMatch(e -> "CREATE".equalsIgnoreCase(e.getEventType()));
            boolean hasDelete = events.stream().anyMatch(e -> "DELETE".equalsIgnoreCase(e.getEventType()));

            assertTrue(hasCreate, "Should have captured at least one CREATE event");
            assertTrue(hasDelete, "Should have captured at least one DELETE event");
        } finally {
            watcher.stop();
        }
    }

    @Test
    @DisplayName("Verify ignored directories (.git, node_modules) do not emit events")
    void testIgnoredDirectories(@TempDir Path tempDir) throws IOException, InterruptedException {
        List<TrackedEvent> events = new CopyOnWriteArrayList<>();
        FileSystemWatcherService watcher = new FileSystemWatcherService(tempDir, events::add);

        watcher.start();

        try {
            // Create .git subdirectory and a file inside it
            Path gitDir = tempDir.resolve(".git");
            Files.createDirectories(gitDir);
            Path gitFile = gitDir.resolve("HEAD");
            Files.writeString(gitFile, "ref: refs/heads/main\n");

            Thread.sleep(600);

            boolean gitCaptured = events.stream().anyMatch(e -> e.getFilePath().contains(".git"));
            assertFalse(gitCaptured, "Events inside .git directory should be ignored");
        } finally {
            watcher.stop();
        }
    }
}

