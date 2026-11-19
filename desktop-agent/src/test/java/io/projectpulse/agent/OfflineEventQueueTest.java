package io.projectpulse.agent;

import io.projectpulse.agent.model.TrackedEvent;
import io.projectpulse.agent.queue.OfflineEventQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OfflineEventQueueTest {

    private File tempQueueFile;
    private OfflineEventQueue queue;

    @BeforeEach
    void setUp() throws Exception {
        tempQueueFile = File.createTempFile("test_queue", ".json");
        tempQueueFile.deleteOnExit();
        queue = new OfflineEventQueue(tempQueueFile);
    }

    @AfterEach
    void tearDown() {
        if (tempQueueFile.exists()) {
            tempQueueFile.delete();
        }
    }

    @Test
    void enqueueAndPollBatch_PreservesOrder() {
        TrackedEvent e1 = new TrackedEvent(UUID.randomUUID().toString(), "CREATE", "src/App.java", null, 1024L, Instant.now());
        TrackedEvent e2 = new TrackedEvent(UUID.randomUUID().toString(), "MODIFY", "src/App.java", null, 1050L, Instant.now());

        queue.enqueue(e1);
        queue.enqueue(e2);

        assertEquals(2, queue.size());
        assertFalse(queue.isEmpty());

        List<TrackedEvent> batch = queue.pollBatch(10);
        assertEquals(2, batch.size());
        assertEquals(e1.getClientEventId(), batch.get(0).getClientEventId());
        assertEquals(e2.getClientEventId(), batch.get(1).getClientEventId());
        assertTrue(queue.isEmpty());
    }

    @Test
    void requeueFront_PreservesTimelineOrder() {
        TrackedEvent e1 = new TrackedEvent(UUID.randomUUID().toString(), "CREATE", "A.txt", null, 10L, Instant.now());
        TrackedEvent e2 = new TrackedEvent(UUID.randomUUID().toString(), "MODIFY", "B.txt", null, 20L, Instant.now());

        queue.enqueue(e1);
        queue.enqueue(e2);

        List<TrackedEvent> batch = queue.pollBatch(2);
        assertTrue(queue.isEmpty());

        queue.requeueFront(batch);
        assertEquals(2, queue.size());

        List<TrackedEvent> polledAgain = queue.pollBatch(2);
        assertEquals(e1.getClientEventId(), polledAgain.get(0).getClientEventId());
        assertEquals(e2.getClientEventId(), polledAgain.get(1).getClientEventId());
    }

    @Test
    void persistenceToDiskAndReload_Success() {
        TrackedEvent e1 = new TrackedEvent(UUID.randomUUID().toString(), "CREATE", "C.txt", null, 500L, Instant.now());
        queue.enqueue(e1);

        queue.persistToDisk();
        assertTrue(tempQueueFile.exists());
        assertTrue(tempQueueFile.length() > 0);

        OfflineEventQueue newQueue = new OfflineEventQueue(tempQueueFile);
        assertEquals(1, newQueue.size());
        TrackedEvent recovered = newQueue.pollBatch(1).get(0);
        assertEquals(e1.getClientEventId(), recovered.getClientEventId());
        assertEquals("C.txt", recovered.getFilePath());
    }
}
