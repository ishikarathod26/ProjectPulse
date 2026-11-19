# ProjectPulse Architecture & System Design

## 1. System Overview

ProjectPulse is an enterprise-grade, privacy-first developer activity tracking and team collaboration platform. It is designed for university engineering teams and software engineering organizations.

Key architectural pillars:
1. **Privacy-Preserving Edge Monitoring**: The desktop agent monitors local filesystem events (`CREATE`, `MODIFY`, `DELETE`, `RENAME`) with explicit project folder consent. **No file contents, source code, or intellectual property is ever captured, transmitted, or stored.**
2. **Deterministic Canonical Activity Hashing**: Every ingested activity is canonically serialized (timestamp in ISO-8601 UTC, project ID, device ID, event type, relative file path, event count, and metadata hash) and hashed using SHA-256.
3. **Dual-Layer Integrity Verification**: Activities are persisted in MySQL (for low-latency query, indexing, filtering, and analytics) and anchored onto a Hyperledger Fabric distributed ledger for immutable tamper-evidence.
4. **Multi-Tenant Scoping & Strict RBAC**: Every resource (projects, activities, chat messages, notes, status updates) is strictly scoped to an organization and project with role-based access control.

```
+-----------------------------------------------------------------------------------+
|                              ProjectPulse Platform                                |
+-----------------------------------------------------------------------------------+
                                       |
    +----------------------------------+----------------------------------+
    |                                  |                                  |
    v                                  v                                  v
+-----------------------+   +-----------------------+   +-----------------------+
|  React 18 Dashboard   |   | Java NIO Desktop Agent|   | Hyperledger Fabric CC |
| (Vite, CSS Modules)   |   | (WatchService, Queue) |   | (Go Chaincode Ledger) |
+-----------------------+   +-----------------------+   +-----------------------+
            |                                  |                                  ^
     HTTPS / REST                       HTTPS / REST                              |
            |                                  |                                  |
            +------------------+---------------+                                  |
                               |                                                  |
                               v                                                  |
                     +--------------------+                                       |
                     | Spring Boot 3.3.4  |---------------------------------------+
                     | Backend REST API   |   (Fabric Gateway / SHA-256 Anchor)
                     +--------------------+
                               |
                        JPA / Hibernate
                               |
                               v
                     +--------------------+
                     |  MySQL 8 Database  |
                     |  (12 Core Tables)  |
                     +--------------------+
```

---

## 2. Component Architecture

### A. Frontend (React 18 + Vite)
- **SPA Routing**: React Router v6 with `<ProtectedRoute>` guards separating public (Login, Register) and authenticated views.
- **Context Management**: `AuthContext` provides centralized token handling, automatic JWT decoding, organization switching, and role tracking (`PLATFORM_ADMIN`, `ORGANIZATION_OWNER`, `PROJECT_ADMIN`, `MEMBER`, `VIEWER`).
- **Axios Interceptor**: Intercepts outgoing requests to attach `Authorization: Bearer <jwt>` and intercepts responses to handle 401s (automatic session purge and redirection).
- **Design System**: Vanilla CSS tokens with sleek dark theme palette, glassmorphism cards, micro-interactions, responsive side navigation, and responsive data tables.
- **Views**:
  - `DashboardPage`: High-level velocity stats, active project list, real-time activity feed.
  - `ProjectsPage`: Project directory, creation modal with key identifier generation, status filtering, and archive actions.
  - `ProjectDetailPage`: Deep-dive project view, member roster with role badges, paginated activity log, and blockchain verification modal.
  - `ActivityPage`: Global organization-wide activity feed with date range and event type filtering.
  - `CollaborationPage`: Integrated project chat, structured notes with activity linkage, and team status updates.
  - `BlockchainPage`: Cryptographic audit interface showing SHA-256 hashes, transaction IDs, block heights, and honest verification statuses (`VERIFIED`, `PENDING`, `MISMATCH`, `BLOCKCHAIN_UNAVAILABLE`).
  - `SettingsPage`: Profile overview, organization member invitations and role management, and desktop agent pairing tokens.
  - `AdminPage`: Platform-wide administrative overview for `PLATFORM_ADMIN` with user suspension controls, organization listings, and system audit logs.

### B. Backend (Spring Boot 3.3.4 + Java 21)
- **Architecture**: Domain-driven layered structure:
  - `auth`: Registration, authentication, JWT token provider, security filter chain, current user resolver.
  - `organization`: Multi-tenant organization CRUD, member management, invitation acceptance, organization switching.
  - `project`: Project lifecycle (Active, Paused, Completed, Archived), membership authorization, role enforcement.
  - `device`: Desktop agent device registration, pairing token validation, last-seen heartbeat.
  - `activity`: Batch activity ingestion, idempotency via composite unique key (`device_id` + `client_assigned_uuid`), canonical SHA-256 hash generation, multi-criteria filtering, and pagination.
  - `collaboration`: Project chat messages, activity-linked notes, status broadcasts.
  - `blockchain`: Canonical serializer, SHA-256 digest calculation, Fabric Gateway client abstraction, honest verification state resolver.
  - `admin`: Platform-wide health metrics, user account activation/suspension, audit trail logging.
- **Security**: Stateless Spring Security with `JwtAuthenticationFilter`, custom `UserDetailsService`, password encoding via `BCryptPasswordEncoder` (strength 12), and role-based pre-authorization.
- **Data Access**: Spring Data JPA with Hibernate, custom JPQL queries, connection pooling via HikariCP, and Flyway versioned migrations.

### C. Desktop Tracking Agent (Java 21 + NIO WatchService)
- **Filesystem Watcher**: Cross-platform recursive filesystem monitoring utilizing `java.nio.file.WatchService`. Watches for `ENTRY_CREATE`, `ENTRY_MODIFY`, `ENTRY_DELETE`, and file renames.
- **Privacy Engine**: Only relative file paths (`src/main/App.java`), event types, and event counts are recorded. Excludes ignored patterns (`.git`, `node_modules`, `target`, `.idea`, build artifacts).
- **Offline Resilience**: `OfflineEventQueue` backed by a local JSON file (`.projectpulse-queue.json`). If the backend is unreachable, events are safely buffered and retried using exponential backoff without dropping data.
- **Idempotency**: Every generated event carries a unique client UUID (`UUID.randomUUID()`). The backend deduplicates any retried events safely.
- **User Interface**: Swing GUI (`AgentMainWindow`) with status indicators (Connected, Paused, Offline, Synchronizing), monitored path selector, pairing token configuration, and real-time event log.

### D. Blockchain Layer (Hyperledger Fabric 2.5 LTS)
- **Chaincode (`activitytracker`)**: Written in Go 1.20 using the Fabric Contract API (`fabric-contract-api-go`).
- **Ledger Records**:
  ```go
  type ActivityRecord struct {
      ActivityID        string `json:"activityId"`
      ProjectID         string `json:"projectId"`
      CanonicalHash     string `json:"canonicalHash"`
      AnchoredTimestamp string `json:"anchoredTimestamp"`
      AnchoredBy        string `json:"anchoredBy"`
  }
  ```
- **Idempotent Anchoring**: `AnchorActivity(ctx, activityId, projectId, canonicalHash, timestamp)` verifies if an anchor already exists. If the existing hash matches, it safely returns the existing transaction; if different, it emits a cryptographic mismatch warning.
- **Verification Query**: `VerifyActivity(ctx, activityId, canonicalHash)` returns boolean match and the immutable ledger record for trustless auditing.
