<#
.SYNOPSIS
  Tears down the ProjectPulse Hyperledger Fabric blockchain containers and volumes.
#>

Write-Host "Tearing down Hyperledger Fabric network..." -ForegroundColor Yellow
if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose -f "$PSScriptRoot\docker-compose-test-net.yml" down -v --remove-orphans
    Write-Host "Fabric containers removed." -ForegroundColor Green
}
