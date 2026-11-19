# ProjectPulse Setup & Installation Guide

This guide walks you through setting up and running all ProjectPulse services locally on Windows, macOS, or Linux.

---

## 1. Prerequisites

- **Java**: JDK 21 LTS (`java -version` should show 21+)
- **Maven**: 3.9+ (or use the included wrapper/tool in `scripts/tools/apache-maven-3.9.6`)
- **Node.js**: v18+ (tested with v24.19.0) & npm 9+
- **MySQL Server** (Optional for dev, required for production): MySQL 8.0+
- **Docker & Docker Compose** (Optional, for Hyperledger Fabric test network)

---

## 2. Environment Configuration

Copy `.env.example` to `.env` or set environment variables:

```bash
# Backend port
SERVER_PORT=8080

# Database (Production / MySQL)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/projectpulse?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_secure_password

# JWT Token Secret (Min 256 bits / 32 characters)
JWT_SECRET=projectpulse-secure-jwt-secret-key-replace-in-production-min-256-bits!
JWT_EXPIRATION_HOURS=24

# Hyperledger Fabric
FABRIC_ENABLED=false
FABRIC_ORG_MSP=Org1MSP
FABRIC_CHANNEL=mychannel
FABRIC_CHAINCODE=activitytracker
```

> **Development Mode Note**: The backend defaults to an embedded in-memory H2 database configured in MySQL-compatibility mode (`spring.profiles.active=dev`), allowing instant startup with no external MySQL setup required.

---

## 3. Database Setup (MySQL)

To use real MySQL:
1. Log in to your MySQL terminal or workbench:
   ```sql
   CREATE DATABASE projectpulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'projectpulse'@'localhost' IDENTIFIED BY 'PulseSecurePass2026!';
   GRANT ALL PRIVILEGES ON projectpulse.* TO 'projectpulse'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. In `backend/src/main/resources/application.properties` (or via environment variables), set:
   ```properties
   spring.profiles.active=prod
   spring.datasource.url=jdbc:mysql://localhost:3306/projectpulse?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=projectpulse
   spring.datasource.password=PulseSecurePass2026!
   ```
3. When the backend starts, Flyway will automatically execute `V1__initial_schema.sql` and verify migration health.

---

## 4. Starting the Backend

From the project root:

### Windows (PowerShell):
```powershell
cd backend
& "..\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

### Alternatively, run the compiled JAR:
```powershell
java -jar backend/target/projectpulse-backend-1.0.0.jar
```

The API server will listen at `http://localhost:8080`.
Verify via health check:
```bash
curl http://localhost:8080/api/auth/me
```

---

## 5. Starting the Frontend Dashboard

From the project root:

### Windows (PowerShell):
```powershell
cd frontend
npm.cmd run dev
```

### Linux / macOS:
```bash
cd frontend
npm run dev
```

The Vite dev server will start at `http://localhost:5173`.
Open `http://localhost:5173` in your browser.

---

## 6. Running the Desktop Tracking Agent

From the project root:

### Windows (PowerShell):
```powershell
cd desktop-agent
java -jar target/projectpulse-desktop-agent-1.0.0.jar
```

Or pass CLI flags:
```powershell
java -jar target/projectpulse-desktop-agent-1.0.0.jar --server http://localhost:8080 --token <pairing-token> --dir "C:\path\to\my-project"
```

---

## 7. Starting Hyperledger Fabric Network (Optional)

If Docker is running and you wish to run the real local blockchain:

### Windows (PowerShell):
```powershell
cd blockchain-network
.\start-network.ps1
.\deploy-chaincode.ps1
```

### Linux / macOS:
```bash
cd blockchain-network
./start-network.sh
./deploy-chaincode.sh
```

When Fabric is started, enable it in the backend:
```properties
projectpulse.blockchain.fabric-enabled=true
```
