# ProjectPulse 🚀
### Project Activity Tracking and Collaboration Platform

ProjectPulse is a project collaboration platform designed for college students and professional software teams. It aims to make project development more transparent by tracking file activity, maintaining version history, and helping team members understand changes made across a shared project.

This project is being developed incrementally as a hands-on software engineering project, with a focus on Java backend development, database design, and full-stack integration.

> **Status:** Under active development.

## 📌 Table of Contents

- [Problem Statement](#-problem-statement)
- [Project Objectives](#-project-objectives)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Database Design](#-database-design)
- [Current Implementation](#-current-implementation)
- [Development Roadmap](#-development-roadmap)
- [Getting Started](#-getting-started)
- [Repository Structure](#-repository-structure)
- [Security Considerations](#-security-considerations)
- [Future Scope](#-future-scope)
- [Author](#-author)

## 🎯 Problem Statement

In collaborative software projects, it can be difficult to understand who changed a file, when the change occurred, and how that change affects the overall project. Existing development workflows may require team members to switch between multiple tools to understand project activity and review changes.

ProjectPulse aims to provide a centralized platform for project activity visibility, file history, and team collaboration.

## 🎯 Project Objectives

- Track project file activity and changes.
- Maintain a history of file versions and associated activity.
- Improve transparency and accountability among team members.
- Support project-based collaboration and membership management.
- Enable team members to review proposed changes before integrating them into an approved project version.
- Explore file integrity verification using cryptographic hashes.

## ✨ Key Features

The following features describe the planned product scope. Features will be marked as implemented only after they have been developed and tested.

- **Project Management:** Organize work into projects and organizations.
- **Team Collaboration:** Manage project memberships and access.
- **File Activity Tracking:** Capture relevant file events through a planned desktop tracking agent.
- **Version History:** Maintain historical versions of tracked files.
- **Change Review:** Support proposed changes and review workflows.
- **Comments and Notifications:** Enable discussion around project files and changes.
- **Integrity Verification:** Explore SHA-256 hashes to detect unexpected changes.
- **Cross-Platform Support:** Target Windows, macOS, and Linux.
- **Blockchain Integration:** Explore Hyperledger Fabric for tamper-evident integrity records.

## 🛠 Technology Stack

| Component | Technology |
|---|---|
| Backend | Java, Spring Boot |
| API | REST APIs |
| Security | Spring Security; authentication implementation in progress |
| Database | MySQL |
| ORM and Persistence | Spring Data JPA, Hibernate |
| Frontend | React, JavaScript, HTML, CSS |
| Desktop File Monitoring | Java NIO WatchService (planned) |
| Integrity Verification | SHA-256 (planned) |
| Blockchain | Hyperledger Fabric (planned) |
| Version Control | Git and GitHub |
| Development Environment | Visual Studio Code |

## 🏗 System Architecture

The planned system will consist of the following components:

1. **Frontend:** A React-based interface for project information, team activity, and collaboration.
2. **Backend:** A Spring Boot application responsible for business logic and REST endpoints.
3. **Database:** MySQL for users, organizations, projects, memberships, file versions, activity events, and related records.
4. **Desktop Tracking Agent:** A planned component that monitors selected project folders and reports relevant file activity to the backend.
5. **Integrity Layer:** A planned mechanism for file hashing and, potentially, blockchain-backed integrity records.

### Planned Workflow

1. A user accesses a project through the platform.
2. Authorized team members work on files in their local project folder.
3. The tracking agent detects relevant file activity.
4. Activity and version information are sent to the backend.
5. The backend validates and stores the relevant records.
6. Team members can review the project history and proposed changes through the frontend.

This workflow describes the intended design; the complete end-to-end system is not implemented yet.

## 🗄 Database Design

The MySQL database is designed around the following entities:

- Users
- Organizations
- Organization Memberships
- Projects
- Project Memberships
- Devices
- Project Files
- File Versions
- Activity Events
- Invitations
- Integrity Anchors
- Change Requests
- Change Request Comments
- File Comments
- Notifications
- Audit Logs

These tables are intended to support identity, project organization, activity tracking, collaboration, version history, and integrity verification.

## ✅ Current Implementation

The initial backend foundation includes:

- Spring Boot project setup.
- MySQL datasource configuration.
- Initial `User` and `Project` entity classes.
- `UserRepository` and `ProjectRepository` interfaces.
- Initial service-layer classes.
- A health endpoint at `GET /api/health`.
- An information endpoint at `GET /api/info`.
- An initial projects endpoint at `GET /api/projects`.
- A BCrypt password encoder bean configuration.
- Initial GitHub repository and version-control setup.

**Note:** These are initial backend components. User registration, complete authentication and authorization, end-to-end file tracking, and the frontend workflow still require implementation and testing.

## 🗺 Development Roadmap

- [x] Initialize the Spring Boot backend.
- [x] Configure the initial MySQL connection.
- [x] Create initial user and project entity/repository structures.
- [x] Add basic health and information endpoints.
- [x] Set up the GitHub repository.
- [ ] Implement and test user registration.
- [ ] Implement authentication and authorization.
- [ ] Implement organization and project membership workflows.
- [ ] Develop the React frontend.
- [ ] Build the desktop file-tracking agent.
- [ ] Implement file version history and activity events.
- [ ] Implement change requests and review workflows.
- [ ] Add comments and notifications.
- [ ] Implement and test SHA-256 integrity verification.
- [ ] Evaluate Hyperledger Fabric integration.
- [ ] Test the complete system across supported operating systems.

## 🚀 Getting Started

### Prerequisites

Install the following tools before running the backend:

- Java Development Kit (JDK)
- Maven, or use the included Maven Wrapper
- MySQL Server
- Git
- Visual Studio Code or another Java-compatible IDE

### 1. Clone the repository

```bash
git clone https://github.com/ishikarathod26/ProjectPulse.git
cd ProjectPulse
```

### 2. Create the MySQL database

Open MySQL Workbench or a MySQL client and run:

```sql
CREATE DATABASE projectpulse;
```

The application expects a MySQL database named `projectpulse`.

### 3. Configure database credentials

Set the `DB_PASSWORD` environment variable to your local MySQL password. Do not commit passwords or other secrets to GitHub.

For Windows PowerShell, for the current terminal session:

```powershell
$env:DB_PASSWORD = "YOUR_LOCAL_MYSQL_PASSWORD"
```

Replace the placeholder with your actual local password. Never paste your real password into this README or a public source file.

### 4. Run the backend

Open a terminal in the backend project directory:

```bash
cd backend/projectpulse-backend
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

If the Maven Wrapper is unavailable, install Maven and use:

```bash
mvn spring-boot:run
```

The application uses the configured Spring Boot port, which is currently expected to be `8080`.

### 5. Check the endpoints

When the application starts successfully, try the following URLs:

- Health: `http://localhost:8080/api/health`
- Information: `http://localhost:8080/api/info`
- Projects: `http://localhost:8080/api/projects`

Spring Security configuration may require authentication for these endpoints until the security rules are configured. A successful application startup also requires the MySQL connection to work.

## 📁 Repository Structure

```text
ProjectPulse/
├── .github/
├── backend/
│   └── projectpulse-backend/
│       ├── mvnw
│       ├── mvnw.cmd
│       ├── pom.xml
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   │   └── com/projectpulse/backend/
│           │   │       ├── config/
│           │   │       ├── controller/
│           │   │       ├── dto/
│           │   │       ├── entity/
│           │   │       ├── repository/
│           │   │       └── service/
│           │   └── resources/
│           │       └── application.properties
│           └── test/
├── frontend/
└── .gitignore
```

The structure may evolve as new modules and features are added.

## 🔐 Security Considerations

- Passwords should be stored as secure hashes, never as plain text.
- Secrets and database credentials must remain outside version control.
- API endpoints must enforce appropriate authentication and authorization.
- Project and organization data should only be accessible to authorized members.
- Security-sensitive functionality must be tested before production use.

## 🔮 Future Scope

- Real-time project activity dashboards.
- Desktop file monitoring for Windows, macOS, and Linux.
- File version comparison and recovery workflows.
- Review and approval workflows for proposed changes.
- Team notifications and collaboration tools.
- Cryptographic integrity verification.
- Evaluation of blockchain-based audit records.
- Automated testing and deployment pipelines.

## 👩‍💻 Author

**Ishika Rathod**

Aspiring Java Developer | B.Tech Computer Science and Information Technology

Currently working on ProjectPulse to develop practical experience in Java, Spring Boot, MySQL, REST API development, and full-stack software engineering.

## 📄 License

No license has been added yet. Licensing will be decided as the project develops.
