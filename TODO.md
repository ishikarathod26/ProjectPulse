# ProjectPulse — Development Checklist & Roadmap

Last Updated: November 2, 2026

## 1. Core Platform Capabilities

- [x] **Authentication & User Management**
  - [x] User registration with BCrypt password hashing
  - [x] JWT token generation and authentication filter
  - [x] User profile retrieval (`/api/auth/me`)
  - [x] Multi-tenant organization switching (`/api/auth/switch-org/{orgId}`)
  - [x] Password validation and error handling

- [x] **Organizations & Multi-Tenancy**
  - [x] Organization creation upon registration
  - [x] Organization member invitations and listing
  - [x] Relational tenant scoping across all entity queries
  - [x] Multi-tenant data isolation test verification

- [x] **Projects & Teams**
  - [x] Project creation, editing, deletion, and archiving
  - [x] Unique project key validation within an organization
  - [x] Project member assignment and removal
  - [x] Role-based permissions (`PLATFORM_ADMIN`, `ORGANIZATION_OWNER`, `PROJECT_ADMIN`, `MEMBER`, `VIEWER`)

- [x] **Desktop Tracking Agent**
  - [x] Cross-platform Java 21 filesystem watcher using NIO `WatchService`
  - [x] Real-time file creation, modification, and deletion detection
  - [x] Ignore patterns for `.git`, `node_modules`, `target`, `.idea`, build artifacts
  - [x] Strict privacy enforcement (metadata only, zero file contents transmitted)
  - [x] Interactive Swing GUI with configuration inputs and live activity log
  - [x] Headless CLI daemon mode with graceful shutdown hooks
  - [x] Offline event queue with persistent disk buffering and auto-retry
  - [x] Shaded executable JAR packaging

- [x] **Activity Ingestion & Integrity**
  - [x] Batch ingestion endpoint (`/api/activities/batch`)
  - [x] Deduplication and idempotency on `(device_id, client_event_id)`
  - [x] Path traversal security defense against malicious paths
  - [x] Canonical SHA-256 activity hashing
  - [x] Timestamp canonicalization to epoch seconds across database drivers

- [x] **Team Collaboration**
  - [x] Real-time / persistent project team chat
  - [x] Collaborator notes linked to specific file activities
  - [x] High-level project status and milestone updates

- [x] **Hyperledger Fabric Blockchain Integration**
  - [x] Go smart contract (`activitytracker.go`)
  - [x] Gateway service with connection profile support
  - [x] Honest status reporting (`VERIFIED`, `PENDING`, `MISMATCHED`, `UNAVAILABLE`)
  - [x] Fallback simulated anchoring when Docker/Fabric is offline
  - [x] Network startup and deployment scripts (`start-network.ps1`, `deploy-chaincode.ps1`)

- [x] **Frontend Web Application**
  - [x] React 18 SPA built with Vite 5 and Tailwind CSS
  - [x] JWT authentication flow with automatic header injection and route guarding
  - [x] Real dynamic dashboard statistics (active projects, events, members, verified hashes)
  - [x] Projects workspace with team assignment and edit modals
  - [x] Activity feed with SHA-256 inspection and live blockchain verification buttons
  - [x] Collaboration hub with chat, notes, and status updates
  - [x] Organization settings and member invitations UI
  - [x] Platform admin console with system stats and audit logs

- [x] **DevOps & Testing**
  - [x] 41/41 Spring Boot backend unit and integration tests passing
  - [x] 7/7 Desktop agent automated tests passing
  - [x] 100% clean production build of React frontend
  - [x] One-click Windows launch scripts (`start-all.bat`, `start-backend.bat`, `start-frontend.bat`, `start-desktop-agent.bat`)
  - [x] Comprehensive documentation (`API.md`, `ARCHITECTURE.md`, `BLOCKCHAIN.md`, `DATABASE.md`, `DESKTOP_AGENT.md`, `SECURITY.md`, `SETUP.md`, `TESTING.md`, Postman collection)

---

## 2. Optional Future Enhancements (Post-Major Project Scope)

- [ ] WebSocket STOMP subscriptions for real-time live chat push updates.
- [ ] Native OS tray minimization for Desktop Agent on Windows / macOS / Linux.
- [ ] Multi-organization consortium setup for Hyperledger Fabric with Raft consensus across distinct cloud providers.
- [ ] Automated CI/CD GitHub Actions workflow for cross-compiling desktop installer binaries (e.g., `.msi`, `.dmg`, `.deb`).

