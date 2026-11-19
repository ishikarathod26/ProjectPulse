# ProjectPulse

> **Cross-Platform Project Activity Tracking & Team Collaboration Platform with Blockchain Integrity Verification**

[![Build Status](https://img.shields.io/badge/Backend-Passing-success)](docs/TESTING.md)
[![Agent Status](https://img.shields.io/badge/Desktop%20Agent-Passing-success)](docs/DESKTOP_AGENT.md)
[![Frontend Status](https://img.shields.io/badge/Frontend-Vite%20React-blue)](docs/SETUP.md)
[![Blockchain](https://img.shields.io/badge/Fabric-2.5%20LTS-purple)](docs/BLOCKCHAIN.md)

---

## 1. Overview

**ProjectPulse** is an enterprise-grade project collaboration and activity tracking platform built for engineering teams and college software projects. It pairs a **Spring Boot REST backend**, a modern **React dashboard**, a **consent-driven Java desktop tracking agent**, and a **Hyperledger Fabric blockchain integrity layer** to provide transparent, verifiable project telemetry without invading developer privacy.

### Key Highlights
- **Privacy-First Telemetry**: The desktop agent monitors only explicitly selected folders and **never reads file contents or uploads source code**.
- **Multi-Tenant Scoping**: Fine-grained role-based access control (`PLATFORM_ADMIN`, `ORGANIZATION_OWNER`, `PROJECT_ADMIN`, `MEMBER`, `VIEWER`).
- **Cryptographic Tamper-Evidence**: SHA-256 canonical hashing of activity logs anchored to Hyperledger Fabric with honest verification states (`VERIFIED`, `PENDING`, `MISMATCHED`, `UNAVAILABLE`).
- **Real-Time Collaboration**: Project chat, activity-linked notes, and status broadcast updates.
- **Zero-Friction Local Run**: Automatic in-memory MySQL-compatible mode for instant developer setup, with production MySQL 8 migration support via Flyway.

---

## 2. Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 18, Vite 5, React Router 6, Axios, Vanilla CSS Design System |
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Security, JWT, Spring Data JPA, Hibernate, Flyway |
| **Desktop Agent** | Java 21 LTS, `java.nio.file.WatchService`, Swing UI + CLI daemon, Jackson JSON, Offline Queue |
| **Database** | MySQL 8.x (Flyway migrations) / In-memory H2 MySQL-compatibility mode |
| **Blockchain** | Hyperledger Fabric 2.5 LTS, Go Chaincode (`activitytracker`), SHA-256 hashing |
| **API & Tools** | OpenAPI 3.0 / Swagger UI, Maven 3.9.6, Node.js 24 |

---

## 3. Repository Structure

```text
ProjectPulse/
├── backend/                  # Spring Boot 3 REST API & Security
│   ├── src/main/java/        # 12 Entities, Controllers, Services, RBAC, Blockchain Client
│   ├── src/main/resources/   # Flyway migrations, application configs
│   ├── src/test/java/        # 27 Unit & Integration test suites
│   └── pom.xml
├── desktop-agent/            # Java 21 Filesystem Monitoring Agent
│   ├── src/main/java/        # WatchService, OfflineEventQueue, Swing UI, CLI
│   ├── src/test/java/        # Queue & Config tests
│   └── pom.xml
├── frontend/                 # Vite + React 18 Web Dashboard
│   ├── src/api/              # Axios client with JWT interceptor & API modules
│   ├── src/components/       # Navbar, Sidebar, ProtectedRoute, Modals
│   ├── src/context/          # AuthContext & organization state
│   ├── src/pages/            # Dashboard, Projects, Activity, Collaboration, Blockchain, Admin
│   └── package.json
├── blockchain-network/       # Hyperledger Fabric 2.5 Assets
│   ├── chaincode/            # Go smart contract (activitytracker.go)
│   ├── docker-compose-*.yml  # Fabric test network containers
│   └── start-network.ps1     # Automation scripts for deployment
├── docs/                     # Comprehensive technical documentation
│   ├── API.md                # REST API Specification
│   ├── ARCHITECTURE.md       # System design & ER diagrams
│   ├── BLOCKCHAIN.md         # Cryptographic verification & chaincode
│   ├── DATABASE.md           # Schema reference & data dictionary
│   ├── DESKTOP_AGENT.md      # Agent architecture & setup guide
│   ├── SECURITY.md           # Threat model, RBAC, & privacy guarantees
│   ├── SETUP.md              # Complete local installation manual
│   ├── TESTING.md            # Test suites & execution instructions
│   └── postman_collection.json # Ready-to-import Postman suite
├── scripts/                  # Utility scripts & portable build tools
│   ├── tools/                # Apache Maven 3.9.6 distribution
│   └── schema.sql            # Direct SQL script for MySQL
├── .env.example              # Sample environment variables
├── DEVELOPMENT_STATUS.md     # Verified module progress tracker
└── README.md                 # Master Project Documentation
```

---

## 4. Quickstart Guide

### Prerequisites
- **Java 21 LTS**: `java -version`
- **Node.js**: v18+ (tested on v24.19.0) & npm
- **Apache Maven**: 3.9+ (included in `scripts/tools/apache-maven-3.9.6/bin/mvn.cmd`)
- **MySQL 8.0+** (Optional for dev; H2 embedded mode is enabled by default)
- **Docker Desktop** (Optional; required only for running Hyperledger Fabric)

---

### Step 1: Start the Backend

Open a terminal in the project directory:

```powershell
# Using the included Maven 3.9.6 wrapper:
.\ProjectPulse\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f .\ProjectPulse\backend\pom.xml spring-boot:run
```

- Backend API: `http://localhost:8080`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON Spec: `http://localhost:8080/v3/api-docs`
- Default In-Memory H2 Console: `http://localhost:8080/h2-console`
- Bootstrap admin credentials must be configured using environment variables before running the application.

To run with real MySQL:
```powershell
java -jar .\ProjectPulse\backend\target\projectpulse-backend-1.0.0.jar --spring.profiles.active=mysql
```

---

### Step 2: Start the Frontend Dashboard

Open a new terminal:

```powershell
cd .\ProjectPulse\frontend
npm.cmd install
npm.cmd run dev
```

- React Dashboard URL: `http://localhost:5173`
- The Vite development server automatically proxies `/api` requests to `http://localhost:8080`.

---

### Step 3: Launch the Desktop Tracking Agent

Open a terminal to run the desktop tracking agent:

```powershell
# Launch interactive Swing GUI:
java -jar .\ProjectPulse\desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar

# Or run in headless CLI daemon mode:
java -jar .\ProjectPulse\desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar \
  --server=http://localhost:8080 \
  --token=YOUR_JWT_TOKEN \
  --org=1 \
  --project=1 \
  --path=C:\path\to\my-project
```

---

### Step 4: (Optional) Start Hyperledger Fabric

To launch the local 2-peer Hyperledger Fabric test network:

```powershell
cd .\ProjectPulse\blockchain-network
.\start-network.ps1
.\deploy-chaincode.ps1
```

Set `FABRIC_ENABLED=true` in `.env` or application arguments to enable live ledger anchoring.

---

## 5. Verification & Testing

Run all automated test suites to verify project integrity:

```powershell
# Run backend tests (27 unit & integration tests):
.\ProjectPulse\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f .\ProjectPulse\backend\pom.xml test

# Run desktop agent tests (5 tests):
.\ProjectPulse\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f .\ProjectPulse\desktop-agent\pom.xml test

# Build frontend production bundle:
npm.cmd run --prefix .\ProjectPulse\frontend build
```

See [docs/TESTING.md](docs/TESTING.md) for full test metrics and report.

---

## 6. Documentation Index

- [Local Setup & Installation Manual](docs/SETUP.md)
- [REST API Specification](docs/API.md)
- [System Architecture & Multi-Tenancy Design](docs/ARCHITECTURE.md)
- [Database Schema & Entity-Relationship Reference](docs/DATABASE.md)
- [Desktop Agent Guide & Privacy Policy](docs/DESKTOP_AGENT.md)
- [Blockchain & Activity Integrity Specification](docs/BLOCKCHAIN.md)
- [Security Model & RBAC Scoping](docs/SECURITY.md)
- [Testing & Quality Assurance Guide](docs/TESTING.md)
- [Postman API Test Suite](docs/postman_collection.json)

