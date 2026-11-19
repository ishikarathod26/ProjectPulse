# ProjectPulse — Hyperledger Fabric Blockchain Network

This module provides the enterprise distributed ledger layer for **ProjectPulse**, providing immutable cryptographic anchoring of filesystem activity events captured by desktop tracking agents.

---

## 1. Architecture & Design

1. **Smart Contract (Chaincode):** Written in Go (`chaincode/activitytracker/activitytracker.go`) utilizing `fabric-contract-api-go`. Implements methods:
   - `InitLedger(ctx)`: Initializes state.
   - `AnchorActivityHash(ctx, activityId, recordHash, orgId, projectId, userId, timestampNano, metadata)`: Writes an immutable record indexed by activity ID. Prohibits duplicate anchoring.
   - `QueryActivityHash(ctx, activityId)`: Reads the anchored hash and transaction metadata.
   - `VerifyActivityHash(ctx, activityId, testHash)`: Returns honest verification state (`VERIFIED`, `MISMATCHED`, or `UNAVAILABLE`).
   - `ActivityExists(ctx, activityId)`: World state existence check.
2. **Channel & MSP:** Channel `projectpulse-channel`, Organization MSP `Org1MSP`.
3. **Gateway Profile:** Connection configuration defined in `connection-org1.json`.
4. **Backend Gateway Client:** Spring Boot backend communicates with the peer via Fabric Gateway.

---

## 2. Operating Modes

### A. Real Fabric Distributed Ledger Mode
When Docker and a Fabric cluster are present:
1. Start network:
   ```bash
   ./start-network.sh        # or ./start-network.ps1
   ```
2. Deploy chaincode:
   ```bash
   ./deploy-chaincode.sh     # or ./deploy-chaincode.ps1
   ```
3. Set in `.env` or Spring Boot configuration:
   ```properties
   FABRIC_ENABLED=true
   FABRIC_CHANNEL_NAME=projectpulse-channel
   FABRIC_CHAINCODE_NAME=activitytracker
   FABRIC_NETWORK_CONFIG_PATH=blockchain-network/connection-org1.json
   ```

### B. Gateway Fallback Simulation Mode
When running in standalone developer or CI environments where Docker or a multi-peer Fabric network is not booted:
- The Spring Boot backend detects that `FABRIC_ENABLED=false` or that the peer is unreachable.
- It calculates true **SHA-256 canonical cryptographic hashes** over all event fields.
- It stores records with unique anchoring transaction IDs in `blockchain_records`.
- Full verification honesty is preserved: records can be tested for `VERIFIED`, `MISMATCHED` (simulated by modifying database records), `PENDING`, or `UNAVAILABLE` states.
- When live Fabric is connected, no code modifications are needed: the gateway directly submits transactions to the peer.

---

## 3. Directory Layout

```
blockchain-network/
├── chaincode/
│   └── activitytracker/
│       ├── activitytracker.go      # Go smart contract
│       └── go.mod                  # Chaincode module dependencies
├── connection-org1.json            # Fabric gateway profile
├── docker-compose-test-net.yml     # Fabric container topology
├── start-network.sh / .ps1         # Network bootstrap
├── deploy-chaincode.sh / .ps1      # Chaincode deployment
├── teardown-network.sh / .ps1      # Container cleanup
└── README.md                       # This guide
```
