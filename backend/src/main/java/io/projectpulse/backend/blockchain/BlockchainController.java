package io.projectpulse.backend.blockchain;

import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.blockchain.dto.BlockchainVerifyResponse;
import io.projectpulse.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@Tag(name = "Blockchain Integrity", description = "Cryptographic hash anchoring and tamper detection endpoints")
public class BlockchainController {

    private final VerificationService verificationService;
    private final BlockchainService blockchainService;

    public BlockchainController(VerificationService verificationService, BlockchainService blockchainService) {
        this.verificationService = verificationService;
        this.blockchainService = blockchainService;
    }

    @RequestMapping(value = "/verify/{activityId}", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Cryptographically verify activity record integrity against the blockchain ledger anchor")
    public ResponseEntity<ApiResponse<BlockchainVerifyResponse>> verifyActivity(
            @PathVariable Long activityId,
            @CurrentUser UserPrincipal principal) {
        BlockchainVerifyResponse response = verificationService.verifyActivityIntegrity(activityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    @Operation(summary = "Check Hyperledger Fabric network gateway connectivity status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNetworkStatus() {
        boolean connected = blockchainService.isNetworkConnected();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "networkType", "Hyperledger Fabric 2.5",
                "connected", connected,
                "mode", connected ? "GATEWAY_ONLINE" : "LOCAL_SIMULATED_ANCHOR",
                "channel", "projectpulse-channel",
                "chaincode", "activitytracker"
        )));
    }
}
