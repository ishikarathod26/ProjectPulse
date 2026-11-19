#!/bin/bash
set -euo pipefail

echo "[-] Tearing down Hyperledger Fabric network..."
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
if command -v docker &> /dev/null; then
    docker compose -f "${DIR}/docker-compose-test-net.yml" down -v --remove-orphans
    echo "[+] Fabric network stopped and cleaned."
fi
