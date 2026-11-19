# ProjectPulse Development Status & Roadmap

**Target Workspace:** `Desktop/Major project/ProjectPulse/`  
**Execution Environment:** Windows 11, Java 21 LTS (`jdk-21.0.12.1`), Node.js `v24.19.0`, npm `11.17.0`, Apache Maven 3.9.6 (`scripts/tools/apache-maven-3.9.6/bin/mvn.cmd`).  
**Last Verified Date:** November 2, 2026

---

## 1. Verified Module Status Summary

| Module | Classification | Implementation Details & Artifacts | Verified Status |
|---|---|---|---|
| **A. Authentication & Organizations** | **Complete and verified** | User registration, BCrypt password hashing, JWT HS256 auth, user profile (`/api/auth/me`), org creation, org switching (`/api/auth/switch-org/{orgId}`), member invitations. | Verified via 6 unit tests & 4 E2E integration test scenarios. |
| **B. Projects & Team Management** | **Complete and verified** | Project CRUD, project member assignment, unique key identifier per org, project archiving, role-based scoping. | Verified via 4 unit tests & 2 E2E integration test scenarios. |
| **C. Desktop Tracking Agent** | **Complete and verified** | Java 21 NIO `WatchService`, interactive Swing GUI + headless CLI daemon (`AgentApp.java`), offline queue (`OfflineEventQueue`), device registration, duplicate event suppression, zero source code transmission. | 7/7 automated tests passing (including live filesystem watcher tests for file creation, modification, and deletion). |
| **D. Activity Ingestion & History** | **Complete and verified** | Batch ingestion endpoint (`/api/activities/batch`), unique constraint on `(device_id, client_event_id)` for idempotency, path traversal defense, pagination and filtering. | Verified via 3 unit tests & 3 E2E integration test scenarios. |
| **E. React Dashboard** | **Complete and verified** | Modern responsive React 18 UI built with Vite 5, Axios client with JWT interceptor, Protected Routes, Dashboard metrics, Projects list & detail view, Activity timeline, Collaboration workspace, Blockchain verification view, Settings, and Platform Administration. | Vite production build passes with 0 errors (`npm.cmd run build`). |
| **F. Team Collaboration** | **Complete and verified** | Project chat messages, project notes linked to activities, status updates with timestamps and user attribution, RBAC authorization. | Verified via 3 unit tests & E2E integration test scenario. |
| **G. Blockchain Verification** | **Complete and verified (Integration boundary)** | Deterministic canonical serialization, SHA-256 hashing, honest verification states (`VERIFIED`, `PENDING`, `MISMATCHED`, `UNAVAILABLE`), Go chaincode contract (`activitytracker.go`), network startup and deployment scripts. | 4/4 unit tests & E2E verification passing. Live distributed ledger anchoring requires local Docker daemon. |
| **H. Roles & Permissions** | **Complete and verified** | Server-side RBAC with `PLATFORM_ADMIN`, `ORGANIZATION_OWNER`, `PROJECT_ADMIN`, `MEMBER`, and `VIEWER`. Multi-tenant relational query scoping. | Cross-tenant access rejection verified in E2E integration test. |
| **I. Platform Administration** | **Complete and verified** | System metrics endpoint (`/api/admin/stats`), user suspension and activation, organization overview, audit logs (`/api/admin/audit-logs`), secure admin bootstrap. | Verified via 3 unit tests & E2E integration test scenario. |
| **J. Testing & Reliability** | **Complete and verified** | **41/41 backend tests passing (27 unit + 14 E2E integration tests in ~24s)**; **7/7 desktop agent tests passing in ~7s**; 100% clean builds across backend, agent, and frontend. | Verified via Apache Maven 3.9.6 and Vite compiler. |

---

## 2. Test Execution & Build Verification Metrics

### Backend (`projectpulse-backend`)
- **Build tool**: Apache Maven 3.9.6
- **Test Results**: **41 run, 0 failures, 0 errors, 0 skipped**
  - `ProjectPulseE2EIntegrationTest`: **14 passed** (Full application lifecycle integration)
    - 1. Health endpoint (`/api/health`)
    - 2. User Registration & auto-org initialization (`/api/auth/register`)
    - 3. User Login & JWT issuance (`/api/auth/login`)
    - 4. Current user profile (`/api/auth/me`)
    - 5. Organization creation & workspace switching (`/api/auth/switch-org/{orgId}`)
    - 6. Project CRUD & unique key enforcement (`/api/projects`)
    - 7. Multi-tenant cross-tenant isolation enforcement
    - 8. Desktop device registration & heartbeat (`/api/devices/register`)
    - 9. Activity batch ingestion & duplicate deduplication (`/api/activities/batch`)
    - 10. Path traversal security defense (`../../etc/shadow` rejection)
    - 11. Activity history retrieval & deterministic SHA-256 hash generation
    - 12. Blockchain verification & honest state check (`/api/blockchain/verify/{id}`)
    - 13. Collaboration suite (Chat, activity-linked notes, status updates)
    - 14. Platform administration suite (`/api/admin/stats`, `/api/admin/audit-logs`)
  - `ActivityServiceTest`: 3 passed (batch ingestion, deduplication, path traversal defense)
  - `AdminServiceTest`: 3 passed (stats aggregation, account suspension, audit logging)
  - `AuthServiceTest`: 6 passed (registration, login, JWT issuance, profile, org switch, invalid auth)
  - `HashAndVerificationTest`: 4 passed (SHA-256 canonical hashing, honest verification states)
  - `CollaborationServiceTest`: 3 passed (chat messaging, activity-linked notes, status updates)
  - `OrganizationServiceTest`: 4 passed (org creation, membership invitations, RBAC scoping)
  - `ProjectServiceTest`: 4 passed (project CRUD, key uniqueness, member assignment)
- **Artifact**: `backend/target/projectpulse-backend-1.0.0.jar`

### Desktop Agent (`projectpulse-desktop-agent`)
- **Build tool**: Apache Maven 3.9.6
- **Test Results**: **7 run, 0 failures, 0 errors, 0 skipped**
  - `FileSystemWatcherTest`: 2 passed (Real `java.nio.file.WatchService` file creation, modification, and deletion detection; ignore filters for `.git` and `node_modules`)
  - `AgentConfigTest`: 2 passed (CLI arguments parsing, default fallbacks)
  - `OfflineEventQueueTest`: 3 passed (FIFO queueing, retry buffer, disk persistence)
- **Artifact**: `desktop-agent/target/projectpulse-desktop-agent-1.0.0.jar` (shaded executable uber-JAR)

### Frontend (`projectpulse-frontend`)
- **Build tool**: Vite 5.4.21 / Node.js 24
- **Modules Transformed**: 105 modules
- **Build Status**: 0 errors, production output generated in `frontend/dist/`

---

## 3. Database Schema & Migration Status

- **Migration tool**: Flyway 10.x
- **Active Migration**: `V1__initial_schema.sql`
- **Relational Tables (12 Total)**:
  1. `users`: Credentials, global role, status
  2. `organizations`: Tenant metadata, owner reference
  3. `organization_members`: Member associations with role scoping
  4. `projects`: Project identifiers, organization foreign key
  5. `project_members`: Project-level collaborator assignments
  6. `devices`: Authenticated agent instances
  7. `activities`: Ingested filesystem telemetry (metadata only)
  8. `blockchain_records`: Anchored ledger transaction hashes
  9. `messages`: Project team chat history
  10. `project_notes`: Collaborator notes linked to activities
  11. `status_updates`: High-level milestone and project health updates
  12. `audit_logs`: Platform-level security and audit trail
- **Compatibility**: Verified on both MySQL 8.x and in-memory MySQL-mode H2 for zero-dependency local development.

---

## 4. Discovered Bugs Fixed During Testing

1. **Frontend-to-Backend REST Endpoint Alignment**:
   - `frontend/src/api/index.js` endpoints were updated to match actual backend routes:
     - Projects: `/api/projects?organizationId={id}` and `/api/projects/{projectId}`
     - Activities: `/api/activities?projectId={projectId}` and `/api/activities/batch`
     - Collaboration: `/api/projects/{projectId}/messages`, `/api/projects/{projectId}/notes`, `/api/projects/{projectId}/status-updates`
2. **Timestamp Canonicalization Across Relational Storage**:
   - In `HashService.java`, timestamps were canonicalized to epoch seconds (`eventTimestamp.getEpochSecond()`), eliminating discrepancies caused by database drivers truncating nanoseconds to seconds/milliseconds.
3. **Public Endpoint Scoping in Security Configuration**:
   - Updated `SecurityConfig` to restrict permitAll to `/api/auth/register` and `/api/auth/login`, ensuring `/api/auth/me` and `/api/auth/switch-org/{orgId}` enforce JWT authentication.
4. **Blockchain Verification Endpoint Method Flexibility**:
   - Updated `BlockchainController` to support both `GET` and `POST` methods for `/api/blockchain/verify/{activityId}`.
5. **Surefire ByteBuddy Agent Pipe Hang on Java 21**:
   - Configured `maven-surefire-plugin` with `-javaagent:.../byte-buddy-agent-1.14.19.jar -XX:+EnableDynamicAgentLoading` to statically load the ByteBuddy agent at JVM startup, resolving Windows sandboxed pipe attachment failures.
6. **PowerShell Script Execution Policy (`npm.ps1` block)**:
   - Standardized frontend build scripts to use `npm.cmd`, ensuring non-blocking execution across Windows systems.

---

## 5. Startup Commands Reference

### One-Click Launch (All Services)
Double-click `scripts\start-all.bat` (or execute `.\ProjectPulse\scripts\start-all.ps1` in PowerShell).  
This automatically starts the Spring Boot backend, Vite React frontend, and Java Swing Desktop Agent in three dedicated terminal windows.

### Individual Launch Scripts

#### Backend (Port 8080)
```powershell
# Using launcher script:
.\ProjectPulse\scripts\start-backend.bat

# Or direct command (in-memory H2 dev mode):
java -jar .\ProjectPulse\backend\target\projectpulse-backend-1.0.0.jar --spring.profiles.active=dev

# Or with real MySQL:
java -jar .\ProjectPulse\backend\target\projectpulse-backend-1.0.0.jar --spring.profiles.active=mysql
```

#### Frontend (Port 5173)
```powershell
# Using launcher script:
.\ProjectPulse\scripts\start-frontend.bat

# Or direct command:
cd .\ProjectPulse\frontend
npm.cmd run dev
```

#### Desktop Tracking Agent
```powershell
# Interactive Swing GUI:
.\ProjectPulse\scripts\start-desktop-agent.bat

# Headless CLI Daemon:
java -jar .\ProjectPulse\desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar --server=http://localhost:8080 --token=JWT_TOKEN --org=1 --project=1 --path=C:\my-project
```

#### Hyperledger Fabric Network (Optional - requires Docker)
```powershell
cd .\ProjectPulse\blockchain-network
.\start-network.ps1
.\deploy-chaincode.ps1
```
