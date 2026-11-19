#!/bin/bash
set -euo pipefail

echo "=========================================================="
echo " ProjectPulse Hyperledger Fabric Network Initialization"
echo "=========================================================="

if ! command -v docker &> /dev/null; then
    echo "[!] Docker not detected in PATH. Operating in simulated gateway fallback mode."
    exit 0
fi

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
docker compose -f "${DIR}/docker-compose-test-net.yml" up -d
echo "[+] Fabric network containers launched."
