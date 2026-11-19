# ProjectPulse Desktop Tracking Agent

The **ProjectPulse Desktop Agent** is a lightweight, cross-platform background application written in Java 21. It provides consent-driven, privacy-preserving filesystem activity tracking for software development teams.

---

## 1. Privacy Guarantees

1. **Zero Content Ingestion**: The agent never reads, inspects, or transmits file contents or source code.
2. **Explicit User Consent**: The agent monitors **only** the single project root directory explicitly selected by the user. It never scans personal directories, documents, or external drives.
3. **No Authorship Fallacy**: Filesystem events denote file changes within a project workspace; the platform honestly presents them as activity events and does not claim to prove individual human intellectual authorship.
4. **Built-in Ignore Filters**: Automatically excludes build artifacts, binary folders, version control internals, and dependency trees:
   - `.git/`, `.svn/`, `.hg/`
   - `node_modules/`
   - `target/`, `build/`, `dist/`, `out/`, `bin/`
   - `.idea/`, `.vscode/`
   - Temporary and swap files (`.tmp`, `.swp`, `~*`)

---

## 2. Architecture & Design

```
+-------------------------------------------------------------+
|                 Desktop Agent Architecture                  |
+-------------------------------------------------------------+
                               |
               +---------------+---------------+
               |                               |
               v                               v
      +-----------------+             +-----------------+
      | Swing GUI Window|             | CLI Headless    |
      | (Interactive)   |             | (Daemon Mode)   |
      +-----------------+             +-----------------+
               \                               /
                \                             /
                 v                           v
      +-------------------------------------------------+
      |                AgentConfig                      |
      | (Backend URL, Pairing Token, Monitored Path)    |
      +-------------------------------------------------+
                               |
                               v
      +-------------------------------------------------+
      |             ProjectDirectoryWatcher             |
      |   - java.nio.file.WatchService                  |
      |   - Recursive subdirectory registration         |
      |   - Debouncing & relative path resolution       |
      +-------------------------------------------------+
                               |
                               v
      +-------------------------------------------------+
      |              OfflineEventQueue                  |
      |   - In-memory event buffer                      |
      |   - Disk persistent fallback                    |
      |     (.projectpulse-queue.json)                  |
      |   - Client-generated UUIDs for idempotency      |
      +-------------------------------------------------+
                               |
                               v
      +-------------------------------------------------+
      |                 AgentApiClient                  |
      |   - HTTP/1.1 REST client                        |
      |   - Batch ingestion (/api/activities/batch)     |
      |   - Heartbeat & auto-reconnect                  |
      +-------------------------------------------------+
```

---

## 3. How to Obtain a Pairing Token

1. Log in to the ProjectPulse web dashboard (`http://localhost:5173`).
2. Navigate to **Settings** -> **Desktop Agent & Devices**.
3. Click **Register New Device**, provide a device name (e.g. `Laptop-Ubuntu` or `Desktop-Windows`), and copy the generated pairing token.

---

## 4. Launching the Agent

### GUI Mode (Interactive):
```powershell
cd desktop-agent
java -jar target/projectpulse-desktop-agent-1.0.0.jar
```
The Swing user interface appears:
- Enter Backend URL (default: `http://localhost:8080`)
- Enter Pairing Token
- Click **Browse...** to select your local project folder
- Click **Start Monitoring**

### CLI / Headless Mode:
```powershell
java -jar target/projectpulse-desktop-agent-1.0.0.jar \
  --server http://localhost:8080 \
  --token dvc_pair_9f3a8b2c4e12 \
  --dir "C:\projects\my-app" \
  --project-id 8e367808-f4b7-449e-b7f3-e5757d5ee7d2
```

---

## 5. Offline Queueing and Resilient Retry

If the developer loses network connectivity:
1. The agent continues logging local filesystem events into `OfflineEventQueue`.
2. The queue persists unsent events to `.projectpulse-queue.json` in the user's home folder.
3. Every event carries a UUID assigned at generation time (`clientAssignedUuid`).
4. When connectivity restores, events are dispatched in FIFO batches.
5. Even if an HTTP request timed out after backend persistence, retries with the same `clientAssignedUuid` are recognized as duplicates by the backend and ignored safely without creating redundant activity entries.
