package io.projectpulse.agent.watcher;

import io.projectpulse.agent.model.TrackedEvent;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class FileSystemWatcherService {

    private final Path rootDir;
    private final Consumer<TrackedEvent> eventConsumer;
    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();
    private final ExecutorService watcherExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    // Debounce cache to prevent burst editor duplicate triggers (path -> lastTimestamp)
    private final Map<String, Long> debounceCache = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_MS = 300;

    // Directories to ignore from monitoring
    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", "node_modules", "target", "build", ".idea", ".vscode", ".mvn", "bin", "out", ".next"
    );

    public FileSystemWatcherService(Path rootDir, Consumer<TrackedEvent> eventConsumer) {
        this.rootDir = rootDir;
        this.eventConsumer = eventConsumer;
    }

    public synchronized void start() throws IOException {
        if (running) return;

        this.watchService = FileSystems.getDefault().newWatchService();
        this.running = true;

        // Register root and all existing subdirectories recursively
        registerTree(rootDir);

        watcherExecutor.submit(this::watchLoop);
    }

    public synchronized void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {}
        }
        watcherExecutor.shutdownNow();
        watchKeys.clear();
        debounceCache.clear();
    }

    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (IGNORED_DIRS.contains(dirName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                registerDir(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerDir(Path dir) {
        try {
            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );
            watchKeys.put(key, dir);
        } catch (IOException e) {
            System.err.println("Notice: Could not watch directory: " + dir + " - " + e.getMessage());
        }
    }

    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            Path parentDir = watchKeys.get(key);
            if (parentDir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path fullPath = parentDir.resolve(filename);

                // Relative path calculation
                Path relPath = rootDir.relativize(fullPath);
                String normalizedPath = relPath.toString().replace('\\', '/');

                // Skip ignored sub-paths
                if (isIgnored(normalizedPath)) {
                    continue;
                }

                // If a new directory was created, register it for recursive monitoring
                if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
                    try {
                        registerTree(fullPath);
                    } catch (IOException ignored) {}
                    continue; // Skip emitting folder create events, track files
                }

                // Debounce rapid editor triggers
                long now = System.currentTimeMillis();
                Long last = debounceCache.get(normalizedPath);
                if (last != null && (now - last) < DEBOUNCE_MS && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    continue;
                }
                debounceCache.put(normalizedPath, now);

                String eventTypeStr;
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    eventTypeStr = "CREATE";
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    eventTypeStr = "MODIFY";
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    eventTypeStr = "DELETE";
                } else {
                    continue;
                }

                Long size = null;
                if (!eventTypeStr.equals("DELETE") && Files.exists(fullPath) && !Files.isDirectory(fullPath)) {
                    try {
                        size = Files.size(fullPath);
                    } catch (IOException ignored) {}
                }

                String eventId = UUID.randomUUID().toString();
                TrackedEvent trackedEvent = new TrackedEvent(
                        eventId,
                        eventTypeStr,
                        normalizedPath,
                        null,
                        size,
                        Instant.now()
                );

                try {
                    eventConsumer.accept(trackedEvent);
                } catch (Exception ex) {
                    System.err.println("Error consuming tracked event: " + ex.getMessage());
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
            }
        }
    }

    private boolean isIgnored(String path) {
        for (String ignored : IGNORED_DIRS) {
            if (path.equals(ignored) || path.startsWith(ignored + "/") || path.contains("/" + ignored + "/")) {
                return true;
            }
        }
        return false;
    }

    public boolean isRunning() {
        return running;
    }
}
