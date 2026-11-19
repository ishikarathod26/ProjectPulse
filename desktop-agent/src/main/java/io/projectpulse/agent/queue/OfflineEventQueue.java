package io.projectpulse.agent.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.projectpulse.agent.model.TrackedEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OfflineEventQueue {

    private final Deque<TrackedEvent> queue = new ConcurrentLinkedDeque<>();
    private final ObjectMapper objectMapper;
    private final File queueFile;
    private static final int MAX_QUEUE_SIZE = 10000;

    public OfflineEventQueue() {
        this(new File("agent_queue.json"));
    }

    public OfflineEventQueue(File queueFile) {
        this.queueFile = queueFile;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        loadFromDisk();
    }

    public void enqueue(TrackedEvent event) {
        if (queue.size() >= MAX_QUEUE_SIZE) {
            queue.pollFirst(); // Drop oldest to avoid unbounded memory usage
        }
        queue.addLast(event);
    }

    public List<TrackedEvent> pollBatch(int maxBatchSize) {
        List<TrackedEvent> batch = new ArrayList<>();
        for (int i = 0; i < maxBatchSize; i++) {
            TrackedEvent ev = queue.pollFirst();
            if (ev == null) break;
            batch.add(ev);
        }
        return batch;
    }

    public void requeueFront(List<TrackedEvent> unDelivered) {
        // Re-insert at the front in reverse order to preserve timeline
        for (int i = unDelivered.size() - 1; i >= 0; i--) {
            queue.addFirst(unDelivered.get(i));
        }
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void persistToDisk() {
        if (queue.isEmpty()) {
            if (queueFile.exists()) {
                queueFile.delete();
            }
            return;
        }
        try {
            List<TrackedEvent> snapshot = new ArrayList<>(queue);
            objectMapper.writeValue(queueFile, snapshot);
        } catch (IOException e) {
            System.err.println("Notice: Could not write offline queue to disk: " + e.getMessage());
        }
    }

    private synchronized void loadFromDisk() {
        if (!queueFile.exists() || queueFile.length() == 0) return;
        try {
            List<TrackedEvent> loaded = objectMapper.readValue(queueFile, new TypeReference<List<TrackedEvent>>() {});
            queue.addAll(loaded);
            queueFile.delete(); // Consumed from disk into active memory queue
        } catch (IOException e) {
            System.err.println("Notice: Could not load offline queue from disk: " + e.getMessage());
        }
    }
}
