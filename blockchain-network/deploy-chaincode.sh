#!/bin/bash
set -euo pipefail

CC_NAME="activitytracker"
CC_VERSION="1.0.0"
CHANNEL_NAME="projectpulse-channel"

echo "=========================================================="
echo " Deploying ActivityTracker Chaincode (${CC_VERSION})"
echo "=========================================================="

if ! command -v docker &> /dev/null; then
    echo "[!] Docker not detected. Chaincode is ready in blockchain-network/chaincode/activitytracker/."
    exit 0
fi

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
echo "[+] Deploying ${CC_NAME} to channel ${CHANNEL_NAME} from ${DIR}/chaincode/activitytracker"
