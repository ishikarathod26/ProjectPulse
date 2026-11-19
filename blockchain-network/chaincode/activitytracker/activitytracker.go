package main

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for anchoring and verifying activity telemetry hashes
type SmartContract struct {
	contractapi.Contract
}

// ActivityAnchor represents an immutable activity hash committed to the distributed ledger
type ActivityAnchor struct {
	ActivityID    string `json:"activityId"`
	RecordHash    string `json:"recordHash"`
	OrgID         uint64 `json:"orgId"`
	ProjectID     uint64 `json:"projectId"`
	UserID        uint64 `json:"userId"`
	TimestampNano int64  `json:"timestampNano"`
	TxID          string `json:"txId"`
	AnchoredAt    string `json:"anchoredAt"`
	Metadata      string `json:"metadata"`
}

// VerificationResult represents the honest cryptographic check returned by the contract
type VerificationResult struct {
	ActivityID   string `json:"activityId"`
	Status       string `json:"status"` // VERIFIED or MISMATCHED
	ProvidedHash string `json:"providedHash"`
	AnchoredHash string `json:"anchoredHash"`
	Matches      bool   `json:"matches"`
	TxID         string `json:"txId"`
	AnchoredAt   string `json:"anchoredAt"`
	Message      string `json:"message"`
}

// InitLedger initializes the chaincode state
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("ProjectPulse ActivityTracker Chaincode Initialized.")
	return nil
}

// AnchorActivityHash anchors an activity's canonical SHA-256 hash onto the immutable ledger
func (s *SmartContract) AnchorActivityHash(
	ctx contractapi.TransactionContextInterface,
	activityID string,
	recordHash string,
	orgID uint64,
	projectID uint64,
	userID uint64,
	timestampNano int64,
	metadata string,
) (*ActivityAnchor, error) {

	if activityID == "" {
		return nil, fmt.Errorf("activityID cannot be empty")
	}
	if len(recordHash) != 64 {
		return nil, fmt.Errorf("invalid SHA-256 hash length: expected 64 hex characters, got %d", len(recordHash))
	}

	exists, err := s.ActivityExists(ctx, activityID)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, fmt.Errorf("activity %s has already been anchored on the ledger; duplicate anchoring is prohibited", activityID)
	}

	txID := ctx.GetStub().GetTxID()
	timestamp, err := ctx.GetStub().GetTxTimestamp()
	anchoredTime := time.Now().UTC().Format(time.RFC3339)
	if err == nil && timestamp != nil {
		anchoredTime = time.Unix(timestamp.Seconds, int64(timestamp.Nanos)).UTC().Format(time.RFC3339)
	}

	anchor := ActivityAnchor{
		ActivityID:    activityID,
		RecordHash:    recordHash,
		OrgID:         orgID,
		ProjectID:     projectID,
		UserID:        userID,
		TimestampNano: timestampNano,
		TxID:          txID,
		AnchoredAt:    anchoredTime,
		Metadata:      metadata,
	}

	anchorJSON, err := json.Marshal(anchor)
	if err != nil {
		return nil, fmt.Errorf("failed to serialize anchor record: %w", err)
	}

	err = ctx.GetStub().PutState(activityID, anchorJSON)
	if err != nil {
		return nil, fmt.Errorf("failed to write to ledger state: %w", err)
	}

	// Emit chaincode event for subscribers
	ctx.GetStub().SetEvent("ActivityHashAnchored", anchorJSON)

	return &anchor, nil
}

// QueryActivityHash retrieves the anchored activity record from the ledger
func (s *SmartContract) QueryActivityHash(ctx contractapi.TransactionContextInterface, activityID string) (*ActivityAnchor, error) {
	anchorJSON, err := ctx.GetStub().GetState(activityID)
	if err != nil {
		return nil, fmt.Errorf("failed to read from ledger: %w", err)
	}
	if anchorJSON == nil {
		return nil, fmt.Errorf("no anchored record found for activity %s", activityID)
	}

	var anchor ActivityAnchor
	err = json.Unmarshal(anchorJSON, &anchor)
	if err != nil {
		return nil, fmt.Errorf("failed to deserialize anchor record: %w", err)
	}

	return &anchor, nil
}

// VerifyActivityHash cryptographically checks if a provided digest matches the ledger anchor
func (s *SmartContract) VerifyActivityHash(
	ctx contractapi.TransactionContextInterface,
	activityID string,
	testHash string,
) (*VerificationResult, error) {

	anchor, err := s.QueryActivityHash(ctx, activityID)
	if err != nil {
		return &VerificationResult{
			ActivityID:   activityID,
			Status:       "UNAVAILABLE",
			ProvidedHash: testHash,
			Matches:      false,
			Message:      "Record not found on the blockchain ledger",
		}, nil
	}

	matches := anchor.RecordHash == testHash
	status := "MISMATCHED"
	message := "Tampering detected: Provided hash does not match immutable ledger anchor."
	if matches {
		status = "VERIFIED"
		message = "Cryptographic integrity verified against Hyperledger Fabric immutable ledger."
	}

	return &VerificationResult{
		ActivityID:   activityID,
		Status:       status,
		ProvidedHash: testHash,
		AnchoredHash: anchor.RecordHash,
		Matches:      matches,
		TxID:         anchor.TxID,
		AnchoredAt:   anchor.AnchoredAt,
		Message:      message,
	}, nil
}

// ActivityExists returns true if the specified activity is already anchored on the ledger
func (s *SmartContract) ActivityExists(ctx contractapi.TransactionContextInterface, activityID string) (bool, error) {
	bytes, err := ctx.GetStub().GetState(activityID)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %w", err)
	}
	return bytes != nil, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		fmt.Printf("Error creating activitytracker chaincode: %s\n", err.Error())
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting activitytracker chaincode: %s\n", err.Error())
	}
}
