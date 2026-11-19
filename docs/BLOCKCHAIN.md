# Hyperledger Fabric Blockchain & Activity Integrity Verification

ProjectPulse leverages **Hyperledger Fabric 2.5 LTS** to provide verifiable, tamper-evident proof of developer activity logs.

---

## 1. Cryptographic Principles

### Canonical Activity Serialization
To guarantee that the exact same activity record always produces the identical cryptographic hash regardless of serialization format, the backend canonicalizes the activity attributes in strict deterministic order:

```text
canonical_string = SHA-256(
    "v1:" +
    projectId + ":" +
    deviceId + ":" +
    eventType + ":" +
    normalizedRelativeFilePath + ":" +
    eventCount + ":" +
    iso8601UtcTimestamp
)
```

Example digest:
`e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`

---

## 2. Honest Verification States

ProjectPulse avoids fabricated verification. The dashboard and API explicitly expose 4 honest verification states:

| Status | Meaning | Action Taken |
|---|---|---|
| `VERIFIED` | The canonical SHA-256 hash in MySQL matches the hash anchored in the Hyperledger Fabric ledger record. | Audit confirmed. Transaction ID and block height displayed. |
| `PENDING` | Activity has been ingested into MySQL but has not yet been submitted to or committed by the Fabric network. | Eligible for automated queue anchoring or manual anchor request. |
| `MISMATCH` | The record exists in the ledger, but its anchored hash does not match the database canonical hash. Indicates database alteration or tampering. | Flagged as critical integrity breach. |
| `BLOCKCHAIN_UNAVAILABLE` | The backend is configured without Fabric (`FABRIC_ENABLED=false`) or the peer gateway cannot be reached. | System honestly declares the blockchain network offline; does not claim verification. |

---

## 3. Go Chaincode Contract (`activitytracker`)

The smart contract is located in `blockchain-network/chaincode/activitytracker/activitytracker.go`.

### Smart Contract Methods:

1. **`InitLedger(ctx contractapi.TransactionContextInterface) error`**
   - Initializes the ledger state.
2. **`AnchorActivity(ctx, activityId, projectId, canonicalHash, timestamp, anchoredBy) (*ActivityRecord, error)`**
   - Stores the activity hash keyed by `activityId`.
   - **Idempotency check**: If an anchor already exists:
     - If the existing hash matches `canonicalHash`, returns existing record without duplicate error.
     - If the existing hash differs, returns `ErrHashMismatch`.
3. **`VerifyActivity(ctx, activityId, canonicalHash) (*VerificationResult, error)`**
   - Queries the state for `activityId`.
   - Returns boolean `isMatch` and details.
4. **`GetActivityHistory(ctx, activityId) ([]ActivityHistoryRecord, error)`**
   - Retrieves full ledger modification history via `GetHistoryForKey`.

---

## 4. Deploying the Test Network

### Prerequisites:
- Docker Desktop with WSL2 backend or Linux Docker engine
- Docker Compose v2

### Quickstart (Windows PowerShell):
```powershell
cd blockchain-network
.\start-network.ps1
.\deploy-chaincode.ps1
```

### Quickstart (Linux / macOS):
```bash
cd blockchain-network
./start-network.sh
./deploy-chaincode.sh
```

### Network Topology:
- Orderer: `orderer.example.com` (Raft consensus)
- Peer: `peer0.org1.example.com`
- CouchDB: `couchdb0` state database
- Channel: `mychannel`
- Chaincode: `activitytracker` package version 1.0

---

## 5. Connecting the Backend to Fabric

In `backend/src/main/resources/application.properties` (or environment variables):
```properties
projectpulse.blockchain.fabric-enabled=true
projectpulse.blockchain.connection-profile-path=../blockchain-network/connection-org1.json
projectpulse.blockchain.crypto-path=../blockchain-network/organizations/peerOrganizations/org1.example.com
projectpulse.blockchain.channel-name=mychannel
projectpulse.blockchain.chaincode-name=activitytracker
```
