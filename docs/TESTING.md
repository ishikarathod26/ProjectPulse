# ProjectPulse Quality Assurance & Testing Guide

This document describes the testing strategy, test suites, automated verification results, and instructions for validating all ProjectPulse components.

---

## 1. Test Architecture Overview

ProjectPulse implements a multi-layer testing strategy covering:

1. **Spring Boot End-to-End Integration Suite (`ProjectPulseE2EIntegrationTest`)**:
   - MockMvc HTTP dispatching through the complete Spring Filter Chain (JWT authentication filter, CORS configuration, exception advice).
   - Real database persistence and Flyway migrations against MySQL-compatible engine.
   - 14 full-lifecycle scenarios from user registration and workspace switching to filesystem activity ingestion, path traversal defense, and blockchain integrity checks.
2. **Backend Unit & Service Isolation Suite (7 Test Classes)**:
   - JUnit 5 & Mockito 5 with ByteBuddy agent instrumentation covering service logic, authentication, multi-tenant RBAC, cryptographic hashing, and batch activity ingestion.
3. **Desktop Agent Test Suite (3 Test Classes)**:
   - JUnit 5 testing covering real `java.nio.file.WatchService` file creation/modification/deletion detection, directory ignore filters (`.git`, `node_modules`), offline FIFO event queueing, and configuration parsing.
4. **Frontend Production Build Verification**:
   - Vite + React 18 production build with module graph validation and CSS compilation.

---

## 2. Automated Test Execution & Results

### Backend Automated Test Suite (41 Tests Run, 0 Failures, 100% Passing)

```bash
# Command to run full backend test suite from workspace root:
.\ProjectPulse\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f .\ProjectPulse\backend\pom.xml test
```

| Test Class | Category | Tests Run | Failures | Errors | Skipped | Key Capabilities Verified |
|---|---|---|---|---|---|---|
| `ProjectPulseE2EIntegrationTest` | **E2E Integration** | **14** | **0** | **0** | **0** | **Full lifecycle**: Health, user registration, JWT login, profile, org switching, project CRUD, cross-tenant isolation, device pairing, activity ingestion, duplicate deduplication, path traversal defense, activity history query, blockchain verification, collaboration suite (chat, notes, status updates), platform administration. |
| `ActivityServiceTest` | Unit / Service | 3 | 0 | 0 | 0 | Batch activity ingestion, duplicate client event deduplication, path traversal attack rejection (`../../etc/passwd`). |
| `AdminServiceTest` | Unit / Service | 3 | 0 | 0 | 0 | Platform metrics aggregation, user status suspension/reactivation, audit log recording. |
| `AuthServiceTest` | Unit / Service | 6 | 0 | 0 | 0 | Registration, BCrypt password validation, JWT issuance, organization creation upon signup, org switching, invalid credential rejection. |
| `HashAndVerificationTest` | Unit / Crypto | 4 | 0 | 0 | 0 | Deterministic SHA-256 canonical serialization, honest `VERIFIED`, `PENDING`, `MISMATCHED`, and `UNAVAILABLE` states. |
| `CollaborationServiceTest` | Unit / Service | 3 | 0 | 0 | 0 | Project chat messaging, activity-linked notes CRUD, status broadcast updates. |
| `OrganizationServiceTest` | Unit / Service | 4 | 0 | 0 | 0 | Organization lifecycle, member invitations, role scoping, cross-organization boundary protection. |
| `ProjectServiceTest` | Unit / Service | 4 | 0 | 0 | 0 | Project CRUD, unique key enforcement, member assignment, project archiving. |
| **Total Backend Tests** | | **41** | **0** | **0** | **0** | **100% Passing (Time: ~24s)** |

---

### Desktop Agent Test Suite (7 Tests Run, 0 Failures, 100% Passing)

```bash
# Command to run desktop agent tests:
.\ProjectPulse\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f .\ProjectPulse\desktop-agent\pom.xml test
```

| Test Class | Tests Run | Failures | Errors | Skipped | Key Capabilities Verified |
|---|---|---|---|---|---|
| `FileSystemWatcherTest` | 2 | 0 | 0 | 0 | Real filesystem monitoring via `java.nio.file.WatchService`: detected file creation (`CREATE`), modification (`MODIFY`), and deletion (`DELETE`). Verified `.git` and `node_modules` ignore filters. |
| `AgentConfigTest` | 2 | 0 | 0 | 0 | CLI flag parsing, default parameter fallbacks, validation of server URL and monitored folder. |
| `OfflineEventQueueTest` | 3 | 0 | 0 | 0 | FIFO queue ordering, in-memory buffering during network disconnection, disk queue persistence. |
| **Total Agent Tests** | **7** | **0** | **0** | **0** | **100% Passing (Time: ~7s)** |

---

### Frontend Build Verification

```bash
# Command to build the frontend application:
npm.cmd run --prefix .\ProjectPulse\frontend build
```

- **Output**:
  - `dist/index.html` (1.20 kB)
  - `dist/assets/index-*.css` (9.82 kB)
  - `dist/assets/index-*.js` (298.35 kB)
- **Result**: Zero compilation errors, 105 transformed modules.

---

## 3. Database Migration Testing

The Flyway migration script `V1__initial_schema.sql` was verified in integration tests against an in-memory MySQL-mode database and verified with schema history creation.

Total Tables Verified (12 Tables):
- `users`
- `organizations`
- `organization_members`
- `projects`
- `project_members`
- `devices`
- `activities`
- `blockchain_records`
- `messages`
- `project_notes`
- `status_updates`
- `audit_logs`

---

## 4. Discovered Bugs Fixed During Testing

1. **Frontend API Path Alignments**:
   - `projectApi`: Aligned to backend `/api/projects?organizationId={orgId}` and `/api/projects/{projectId}`.
   - `activityApi`: Aligned to `/api/activities?projectId={projectId}` and `/api/activities/batch`.
   - `collaborationApi`: Aligned to `/api/projects/{projectId}/messages`, `/api/projects/{projectId}/notes`, and `/api/projects/{projectId}/status-updates`.
2. **Timestamp Canonicalization for Deterministic Hashing**:
   - Truncated `eventTimestamp` in `HashService` to epoch seconds (`getEpochSecond()`), ensuring deterministic SHA-256 calculation remains consistent before and after SQL database persistence.
3. **Public Endpoint Scoping**:
   - Updated `SecurityConfig` to restrict permitAll to `/api/auth/register` and `/api/auth/login`, ensuring `/api/auth/me` and `/api/auth/switch-org/**` require proper JWT authentication.
4. **Blockchain Controller HTTP Method Support**:
   - Configured `BlockchainController` to support both GET and POST for `/api/blockchain/verify/{activityId}`.
