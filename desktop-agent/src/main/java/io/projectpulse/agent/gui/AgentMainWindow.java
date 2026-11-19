package io.projectpulse.agent.gui;

import io.projectpulse.agent.model.AgentConfig;
import io.projectpulse.agent.model.TrackedEvent;
import io.projectpulse.agent.net.BackendApiClient;
import io.projectpulse.agent.queue.OfflineEventQueue;
import io.projectpulse.agent.watcher.FileSystemWatcherService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMainWindow extends JFrame {

    private final AgentConfig config;
    private final OfflineEventQueue eventQueue;
    private BackendApiClient apiClient;
    private FileSystemWatcherService watcherService;

    // UI Components
    private JTextField serverUrlField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel authStatusLabel;

    private JComboBox<ProjectItem> projectComboBox;
    private JTextField dirPathField;
    private JButton browseButton;
    private JButton toggleWatchButton;
    private JLabel statusBadge;

    private JLabel statCapturedLabel;
    private JLabel statUploadedLabel;
    private JLabel statQueuedLabel;

    private DefaultTableModel tableModel;
    private JTable eventTable;

    private long totalCaptured = 0;
    private long totalUploaded = 0;

    private final ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();

    public AgentMainWindow(AgentConfig config, OfflineEventQueue eventQueue) {
        this.config = config;
        this.eventQueue = eventQueue;
        this.apiClient = new BackendApiClient(config.getServerUrl(), config.getAuthToken());

        initUi();
        startPeriodicSync();
    }

    private void initUi() {
        setTitle("ProjectPulse Desktop Tracking Agent - v1.0");
        setSize(920, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(248, 250, 252));

        // 1. Header & Connection Bar
        mainPanel.add(buildTopPanel(), BorderLayout.NORTH);

        // 2. Center: Activity Stream Table
        mainPanel.add(buildCenterPanel(), BorderLayout.CENTER);

        // 3. Bottom: Status & Stats Bar
        mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Populate fields from config
        serverUrlField.setText(config.getServerUrl());
        dirPathField.setText(config.getMonitoredDirectory());
    }

    private JPanel buildTopPanel() {
        JPanel container = new JPanel(new GridLayout(2, 1, 8, 8));
        container.setOpaque(false);

        // Row 1: Connection & Login
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.setBackground(Color.WHITE);
        row1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        row1.add(new JLabel("Server:"));
        serverUrlField = new JTextField("http://localhost:8080", 16);
        row1.add(serverUrlField);

        row1.add(new JLabel("Email:"));
        emailField = new JTextField("admin@projectpulse.io", 14);
        row1.add(emailField);

        row1.add(new JLabel("Password:"));
        passwordField = new JPasswordField(10);
        
        row1.add(passwordField);

        loginButton = new JButton("Connect & Login");
        loginButton.setBackground(new Color(79, 70, 229));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> performLogin());
        row1.add(loginButton);

        authStatusLabel = new JLabel("Not authenticated");
        authStatusLabel.setForeground(new Color(100, 116, 139));
        row1.add(authStatusLabel);

        // Row 2: Directory & Watcher Controls
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.setBackground(Color.WHITE);
        row2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        row2.add(new JLabel("Target Project:"));
        projectComboBox = new JComboBox<>();
        projectComboBox.addItem(new ProjectItem(1L, "Default Project (ID: 1)"));
        projectComboBox.setPreferredSize(new Dimension(180, 26));
        row2.add(projectComboBox);

        row2.add(new JLabel("Local Folder:"));
        dirPathField = new JTextField(22);
        dirPathField.setEditable(false);
        row2.add(dirPathField);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> selectFolder());
        row2.add(browseButton);

        toggleWatchButton = new JButton("Start Tracking");
        toggleWatchButton.setBackground(new Color(16, 185, 129));
        toggleWatchButton.setForeground(Color.WHITE);
        toggleWatchButton.setFocusPainted(false);
        toggleWatchButton.setEnabled(false);
        toggleWatchButton.addActionListener(e -> toggleTracking());
        row2.add(toggleWatchButton);

        statusBadge = new JLabel("● IDLE");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setForeground(new Color(148, 163, 184));
        row2.add(statusBadge);

        container.add(row1);
        container.add(row2);
        return container;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Live File System Event Stream");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Timestamp", "Action", "Relative File Path", "Size (bytes)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(24);
        eventTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(400);
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        eventTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(eventTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsRow.setOpaque(false);

        statCapturedLabel = new JLabel("Captured: 0");
        statCapturedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statUploadedLabel = new JLabel("Uploaded: 0");
        statUploadedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statUploadedLabel.setForeground(new Color(16, 185, 129));
        statQueuedLabel = new JLabel("Queued Offline: 0");
        statQueuedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statQueuedLabel.setForeground(new Color(234, 88, 12));

        statsRow.add(statCapturedLabel);
        statsRow.add(statUploadedLabel);
        statsRow.add(statQueuedLabel);

        JLabel deviceLabel = new JLabel("Device ID: " + config.getDeviceIdentifier());
        deviceLabel.setForeground(new Color(148, 163, 184));
        deviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        panel.add(statsRow, BorderLayout.WEST);
        panel.add(deviceLabel, BorderLayout.EAST);
        return panel;
    }

    private void performLogin() {
        String url = serverUrlField.getText().trim();
        String email = emailField.getText().trim();
        String pwd = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        authStatusLabel.setText("Connecting...");

        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                apiClient = new BackendApiClient(url, "");
                return apiClient.login(email, pwd);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    Map<String, Object> result = get();
                    String token = (String) result.get("token");
                    config.setServerUrl(url);
                    config.setAuthToken(token);
                    config.save();

                    authStatusLabel.setText("✓ " + result.get("fullName"));
                    authStatusLabel.setForeground(new Color(16, 185, 129));

                    // Register device
                    apiClient.registerDevice(config.getDeviceIdentifier(), config.getDeviceName(), System.getProperty("os.name"));

                    // Fetch projects
                    loadProjects((Long) result.get("activeOrgId"));

                    toggleWatchButton.setEnabled(dirPathField.getText().length() > 0);
                } catch (Exception ex) {
                    authStatusLabel.setText("Login failed: " + ex.getCause().getMessage());
                    authStatusLabel.setForeground(new Color(239, 68, 68));
                }
            }
        };
        worker.execute();
    }

    private void loadProjects(Long orgId) {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return apiClient.fetchProjects(orgId);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> projects = get();
                    projectComboBox.removeAllItems();
                    for (Map<String, Object> p : projects) {
                        Long id = ((Number) p.get("id")).longValue();
                        String name = (String) p.get("name");
                        String key = (String) p.get("keyIdentifier");
                        projectComboBox.addItem(new ProjectItem(id, name + " (" + key + ")"));
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Project Folder to Monitor");

        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            dirPathField.setText(selected.getAbsolutePath());
            config.setMonitoredDirectory(selected.getAbsolutePath());
            config.save();

            if (apiClient.getAuthToken() != null && !apiClient.getAuthToken().isBlank()) {
                toggleWatchButton.setEnabled(true);
            }
        }
    }

    private void toggleTracking() {
        if (watcherService != null && watcherService.isRunning()) {
            watcherService.stop();
            watcherService = null;
            toggleWatchButton.setText("Start Tracking");
            toggleWatchButton.setBackground(new Color(16, 185, 129));
            statusBadge.setText("● STOPPED");
            statusBadge.setForeground(new Color(148, 163, 184));
            browseButton.setEnabled(true);
        } else {
            String pathStr = dirPathField.getText().trim();
            if (pathStr.isEmpty()) return;

            File folder = new File(pathStr);
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Selected path is not a valid directory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                watcherService = new FileSystemWatcherService(folder.toPath(), this::onFileEventDetected);
                watcherService.start();

                toggleWatchButton.setText("Stop Tracking");
                toggleWatchButton.setBackground(new Color(239, 68, 68));
                statusBadge.setText("● ACTIVE TRACKING");
                statusBadge.setForeground(new Color(16, 185, 129));
                browseButton.setEnabled(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not start watcher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onFileEventDetected(TrackedEvent event) {
        totalCaptured++;
        eventQueue.enqueue(event);

        SwingUtilities.invokeLater(() -> {
            statCapturedLabel.setText("Captured: " + totalCaptured);
            statQueuedLabel.setText("Queued Offline: " + eventQueue.size());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            String timeStr = fmt.format(event.getEventTimestamp().atZone(java.time.ZoneId.systemDefault()));

            tableModel.insertRow(0, new Object[]{
                    timeStr,
                    event.getEventType(),
                    event.getFilePath(),
                    event.getFileSizeBytes() != null ? event.getFileSizeBytes() : "-",
                    "QUEUED"
            });

            if (tableModel.getRowCount() > 500) {
                tableModel.removeRow(tableModel.getRowCount() - 1);
            }
        });
    }

    private void startPeriodicSync() {
        syncExecutor.scheduleWithFixedDelay(() -> {
            if (eventQueue.isEmpty()) return;

            Long selectedProjectId = 1L;
            ProjectItem item = (ProjectItem) projectComboBox.getSelectedItem();
            if (item != null) selectedProjectId = item.id;

            List<TrackedEvent> batch = eventQueue.pollBatch(50);
            if (batch.isEmpty()) return;

            boolean success = apiClient.uploadActivityBatch(selectedProjectId, config.getDeviceIdentifier(), batch);
            if (success) {
                totalUploaded += batch.size();
                SwingUtilities.invokeLater(() -> {
                    statUploadedLabel.setText("Uploaded: " + totalUploaded);
                    statQueuedLabel.setText("Queued Offline: " + eventQueue.size());

                    // Update table status
                    for (int i = 0; i < Math.min(tableModel.getRowCount(), batch.size()); i++) {
                        tableModel.setValueAt("✓ UPLOADED", i, 4);
                    }
                });
            } else {
                // Return batch to queue to retry safely
                eventQueue.requeueFront(batch);
                SwingUtilities.invokeLater(() -> {
                    statQueuedLabel.setText("Queued Offline: " + eventQueue.size());
                });
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    private static class ProjectItem {
        final Long id;
        final String label;

        ProjectItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
