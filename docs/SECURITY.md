# ProjectPulse Security Architecture, Tenant Isolation & Limitations

This document outlines the security architecture, threat model, tenant isolation strategies, privacy controls, and documented limitations of the ProjectPulse platform.

---

## 1. Authentication & Token Management

### Password Hashing
- ProjectPulse enforces the **BCrypt strong hashing algorithm** (strength factor 10+) for all stored credentials.
- Plaintext passwords are never logged, transmitted in error messages, or retained in heap memory.
- Password requirements: Minimum 8 characters, checked on both client and server layers.

### JWT (JSON Web Token) Security
- Stateless token architecture using **HMAC-SHA256 (HS256)** signing.
- Configurable token expiration (default: 24 hours).
- Custom Claims: `userId`, `email`, `systemRole`, and `activeOrgId`.
- Invalidation / Expiration: Token expiry triggers HTTP 401 Unauthorized, automatically prompting client cleanup and redirection.

---

## 2. Multi-Tenant Data Isolation & RBAC

### Role Hierarchy
ProjectPulse enforces a strict 5-tier Role-Based Access Control (RBAC) model:

1. **`PLATFORM_ADMIN`**: Global platform administrator. Can access `/api/admin/**`, view platform-wide stats, audit logs, and suspend/reactivate user accounts. Distinct from organization ownership.
2. **`ORGANIZATION_OWNER`**: Creator or appointed administrator of an Organization. Can invite/remove members, manage organization settings, and create projects.
3. **`PROJECT_ADMIN`**: Administrator within a specific project. Can configure project metadata, manage members within the project, and archive projects.
4. **`MEMBER`**: Active collaborator. Can ingest activities, post chat messages, write project notes, and create status updates.
5. **`VIEWER`**: Read-only stakeholder. Can inspect activity timelines, notes, and verification statuses, but cannot post content or ingest telemetry.

### Tenant Isolation Architecture
Every project entity, activity, chat message, and note is scoped by compound relational keys:
- Cross-tenant queries are structurally prevented by scoping queries to `(organization_id, project_id)`.
- Backend services verify organization membership before allowing access to any child resource.
- Desktop agent pairings require a project-scoped device token and validate project enrollment on each batch ingestion.

---

## 3. Desktop Agent Privacy Guarantees

### Zero Content Ingestion
The ProjectPulse Desktop Tracking Agent is architected with strict privacy guarantees:
- **Never reads or inspects file contents**: Only filesystem metadata (file path, change event type, file size in bytes, and timestamp) is captured.
- **Never uploads source code**: Source code, intellectual property, environment secrets (`.env`), and keys remain strictly local to the developer's computer.

### Explicit Consent & Scoped Monitoring
- Monitoring begins **only** when a developer explicitly selects a directory path in the GUI or via `--path` CLI flag.
- The agent strictly confines `WatchService` registration to the chosen project directory and its subdirectories.
- Built-in ignore filters automatically exclude:
  - Version control internals (`.git/`, `.svn/`, `.hg/`)
  - Dependency trees (`node_modules/`, `vendor/`)
  - Build directories (`target/`, `build/`, `dist/`, `out/`, `bin/`)
  - IDE settings (`.idea/`, `.vscode/`)
  - Temporary & lock files (`.tmp`, `.swp`, `~*`)

---

## 4. Input Validation & Path Traversal Defense

### Defensive File Path Canonicalization
When activity telemetry is ingested:
- The backend normalizes relative file paths, replacing backslashes with standard forward slashes.
- Any file path containing relative directory traversal sequences (`../`, `..\`) is rejected with an `IllegalArgumentException` and logged in audit logs as a path traversal attempt.

### Deduplication & Idempotency
- Each activity payload carries a client-generated UUID (`client_event_id`).
- A unique database constraint on `(device_id, client_event_id)` ensures that network retries by the desktop agent's offline queue never produce duplicate activity rows in MySQL.

---

## 5. Honest Blockchain Verification Policy

ProjectPulse strictly rejects dishonest claims of verification:
- When Hyperledger Fabric is disabled (`FABRIC_ENABLED=false`) or unreachable, the system explicitly reports **`UNAVAILABLE`**. It does not pretend records are verified.
- When an activity has not yet been submitted to the blockchain network, it is classified as **`PENDING`**.
- If a record's recalculated database SHA-256 hash does not match the anchored hash on the ledger, it is flagged as **`MISMATCHED`**.
- Only when the cryptographic hash matches the ledger state does the system return **`VERIFIED`**.

---

## 6. Documented Limitations & Disclaimers

1. **Filesystem Telemetry vs. Human Authorship**:
   - Filesystem change monitoring indicates that a file was created or modified in a monitored directory on an authenticated device.
   - It **does not prove** individual human intellectual authorship or quantify code quality.
2. **Blockchain Network Prerequisite**:
   - Anchoring hashes to a distributed ledger requires a running Hyperledger Fabric peer network with the `activitytracker` chaincode deployed. In standalone development mode, activities are persisted to MySQL with local deterministic SHA-256 digests.
3. **Clock Skew**:
   - Event timestamps are generated on the client machine and recorded alongside server receipt timestamps. Agents should maintain NTP synchronization to ensure chronological accuracy.

