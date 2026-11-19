# ProjectPulse REST API Specification

Base URL: `http://localhost:8080/api`

All authenticated endpoints require the header:
```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

---

## 1. Authentication (`/api/auth`)

### Register
`POST /api/auth/register`
- **Request Body**:
```json
{
  "fullName": "Jane Developer",
  "email": "jane@example.com",
  "password": "Password123!",
  "organizationName": "Acme Labs"
}
```
- **Response** `201 Created`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": "d74f2604-8b6f-4db4-bb14-97c7295aa621",
  "email": "jane@example.com",
  "fullName": "Jane Developer",
  "role": "USER",
  "activeOrganizationId": "2bf5df7e-a0e2-4fec-be10-09325779bfa2",
  "organizations": [
    {
      "id": "2bf5df7e-a0e2-4fec-be10-09325779bfa2",
      "name": "Acme Labs",
      "role": "ORGANIZATION_OWNER"
    }
  ]
}
```

### Login
`POST /api/auth/login`
- **Request Body**:
```json
{
  "email": "jane@example.com",
  "password": "Password123!"
}
```
- **Response** `200 OK`: Same structure as registration response.

### Current User Profile
`GET /api/auth/me`
- **Response** `200 OK`: Current authenticated user details and active organization affiliations.

### Switch Organization
`POST /api/auth/switch-org/{orgId}`
- **Response** `200 OK`: Returns new JWT scoped to the chosen organization.

---

## 2. Organizations (`/api/organizations`)

### List User Organizations
`GET /api/organizations`

### Get Organization by ID
`GET /api/organizations/{orgId}`

### Create Organization
`POST /api/organizations`
- **Request Body**:
```json
{
  "name": "Mobile Dev Team",
  "slug": "mobile-dev-team",
  "description": "Android & iOS core development"
}
```

### List Organization Members
`GET /api/organizations/{orgId}/members`

### Invite Member to Organization
`POST /api/organizations/{orgId}/members/invite`
- **Request Body**:
```json
{
  "email": "colleague@example.com",
  "role": "MEMBER"
}
```

### Update Member Role
`PUT /api/organizations/{orgId}/members/{userId}/role`
- **Request Body**:
```json
{
  "role": "ORGANIZATION_OWNER"
}
```

---

## 3. Projects (`/api/organizations/{orgId}/projects`)

### List Projects
`GET /api/organizations/{orgId}/projects?status=ACTIVE`

### Create Project
`POST /api/organizations/{orgId}/projects`
- **Request Body**:
```json
{
  "name": "Project Alpha",
  "keyIdentifier": "ALPHA",
  "description": "Next-gen platform redesign"
}
```

### Get Project Details & Stats
`GET /api/organizations/{orgId}/projects/{projectId}`

### Update Project Status
`PATCH /api/organizations/{orgId}/projects/{projectId}/status`
- **Request Body**:
```json
{
  "status": "ARCHIVED"
}
```

### Project Members
- `GET /api/organizations/{orgId}/projects/{projectId}/members`
- `POST /api/organizations/{orgId}/projects/{projectId}/members` (Assign user with role)

---

## 4. Desktop Agent & Devices (`/api/devices`)

### Register Device
`POST /api/devices/register`
- **Request Body**:
```json
{
  "deviceName": "MacBook-Pro-M2",
  "osInfo": "macOS 14.1 Sonoma"
}
```
- **Response** `201 Created`:
```json
{
  "deviceId": "57b7a6fc-6b58-4523-be12-9c98fb4208a1",
  "deviceName": "MacBook-Pro-M2",
  "pairingToken": "dvc_pair_7f3918a93bc24e5482e013ab62",
  "createdAt": "2026-11-02T15:00:00Z"
}
```

### Heartbeat
`POST /api/devices/{deviceId}/heartbeat`

---

## 5. Activity Ingestion & History (`/api/activities`)

### Batch Activity Ingestion
`POST /api/activities/batch`
- **Headers**: `X-Device-Token: <pairingToken>` or `Authorization: Bearer <jwt>`
- **Request Body**:
```json
{
  "projectId": "8e367808-f4b7-449e-b7f3-e5757d5ee7d2",
  "deviceId": "57b7a6fc-6b58-4523-be12-9c98fb4208a1",
  "events": [
    {
      "clientAssignedUuid": "c613e548-a003-4952-bdae-aa672954a781",
      "eventType": "FILE_MODIFY",
      "filePath": "src/controllers/UserController.java",
      "eventCount": 4,
      "timestamp": "2026-11-02T15:20:00Z"
    }
  ]
}
```
- **Response** `200 OK`:
```json
{
  "accepted": 1,
  "duplicates": 0,
  "failed": 0
}
```

### Query Project Activities (Paginated)
`GET /api/projects/{projectId}/activities?page=0&size=20&eventType=FILE_MODIFY&startDate=2026-11-01T00:00:00Z&endDate=2026-11-02T23:59:59Z`

---

## 6. Collaboration (`/api/projects/{projectId}/collaboration`)

### Messages (Project Chat)
- `GET /api/projects/{projectId}/collaboration/messages?limit=50`
- `POST /api/projects/{projectId}/collaboration/messages`
  ```json
  { "content": "Updated the auth middleware to handle token expiration" }
  ```

### Project Notes
- `GET /api/projects/{projectId}/collaboration/notes`
- `POST /api/projects/{projectId}/collaboration/notes`
  ```json
  {
    "title": "API Refactor Notes",
    "content": "Migrated from v1 endpoints to v2 standard",
    "activityId": "optional-uuid"
  }
  ```

### Status Updates (Daily Standups)
- `GET /api/projects/{projectId}/collaboration/status-updates`
- `POST /api/projects/{projectId}/collaboration/status-updates`
  ```json
  {
    "statusText": "Working on Fabric chaincode anchoring integration",
    "mood": "ON_TRACK"
  }
  ```

---

## 7. Blockchain Verification (`/api/blockchain`)

### Verify Activity Against Ledger
`GET /api/blockchain/verify/{activityId}`
- **Response** `200 OK`:
```json
{
  "activityId": "e1f2b012-70b5-4122-8394-bb9e16d4187f",
  "canonicalHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "status": "VERIFIED",
  "transactionId": "tx_fa91b5c468e2741d6",
  "blockHeight": 104,
  "anchoredAt": "2026-11-02T15:21:00Z",
  "blockchainMessage": "Ledger state matches canonical hash exactly"
}
```

### Trigger Manual Anchor (Admin/Owner)
`POST /api/blockchain/anchor/{activityId}`

---

## 8. Platform Administration (`/api/admin`)
Requires `PLATFORM_ADMIN` global role.

### Platform Summary Statistics
`GET /api/admin/stats`
- Returns total users, organizations, projects, activities, and anchoring rates.

### User Management
- `GET /api/admin/users?page=0&size=20`
- `PATCH /api/admin/users/{userId}/status`
  ```json
  { "status": "SUSPENDED" }
  ```

### Platform Audit Logs
- `GET /api/admin/audit-logs?page=0&size=50`
