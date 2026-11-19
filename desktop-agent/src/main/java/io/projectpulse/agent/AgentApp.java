package io.projectpulse.agent;

import io.projectpulse.agent.gui.AgentMainWindow;
import io.projectpulse.agent.model.AgentConfig;
import io.projectpulse.agent.queue.OfflineEventQueue;
import io.projectpulse.agent.watcher.FileSystemWatcherService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class AgentApp {

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" ProjectPulse Desktop Tracking Agent v1.0.0");
        System.out.println(" Cross-Platform Consent-Based Filesystem Telemetry");
        System.out.println("=====================================================");

        AgentConfig config = AgentConfig.load();
        OfflineEventQueue eventQueue = new OfflineEventQueue();

        // Register graceful shutdown hook to persist undelivered queue events
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down ProjectPulse Desktop Agent...");
            eventQueue.persistToDisk();
            config.save();
            System.out.println("Agent state persisted cleanly.");
        }));

        boolean headless = GraphicsEnvironment.isHeadless();
        String cliDir = null;
        for (String arg : args) {
            if ("--headless".equalsIgnoreCase(arg)) {
                headless = true;
            } else if (arg.startsWith("--dir=")) {
                cliDir = arg.substring(6).trim();
            } else if (arg.startsWith("--server=")) {
                config.setServerUrl(arg.substring(9).trim());
            }
        }

        if (headless) {
            runHeadless(config, eventQueue, cliDir);
        } else {
            runGui(config, eventQueue);
        }
    }

    private static void runGui(AgentConfig config, OfflineEventQueue eventQueue) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored2) {}
        }

        SwingUtilities.invokeLater(() -> {
            AgentMainWindow mainWindow = new AgentMainWindow(config, eventQueue);
            mainWindow.setVisible(true);
        });
    }

    private static void runHeadless(AgentConfig config, OfflineEventQueue eventQueue, String cliDir) {
        System.out.println("Running in Headless / Service Mode.");
        System.out.println("Server URL: " + config.getServerUrl());
        System.out.println("Device ID: " + config.getDeviceIdentifier());

        String targetDir = cliDir != null ? cliDir : config.getMonitoredDirectory();
        if (targetDir == null || targetDir.isBlank()) {
            System.out.println("No monitored directory configured. Pass --dir=/path/to/project or set in config.");
            return;
        }

        File folder = new File(targetDir);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Target directory does not exist or is not a directory: " + targetDir);
            return;
        }

        System.out.println("Starting filesystem monitoring on: " + folder.getAbsolutePath());
        try {
            FileSystemWatcherService watcher = new FileSystemWatcherService(folder.toPath(), event -> {
                System.out.printf("[%s] %s (%s) %d bytes%n",
                        event.getEventType(),
                        event.getFilePath(),
                        event.getClientEventId(),
                        event.getFileSizeBytes() != null ? event.getFileSizeBytes() : 0);
                eventQueue.enqueue(event);
            });
            watcher.start();
            System.out.println("Watcher active. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Error running headless watcher: " + e.getMessage());
        }
    }
}
