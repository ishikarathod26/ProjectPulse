package io.projectpulse.backend.blockchain;

public interface BlockchainService {
    
    /**
     * Submits an activity hash to the blockchain anchor.
     * @param activityId the database ID of the activity
     * @param recordHash the SHA-256 canonical hash
     * @param metadata canonical metadata summary
     * @return BlockchainRecord with updated status and transaction ID
     */
    BlockchainRecord anchorActivityHash(Long activityId, String recordHash, String metadata);

    /**
     * Retrieves the anchored hash recorded on the blockchain ledger for a given activity.
     * @param activityId the activity ID
     * @return the recorded hash or null if not anchored
     */
    String queryAnchoredHash(Long activityId);

    /**
     * Checks if the blockchain gateway is active and connected.
     */
    boolean isNetworkConnected();
}
