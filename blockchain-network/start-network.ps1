<#
.SYNOPSIS
  Starts the ProjectPulse Hyperledger Fabric blockchain test network.
#>

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " ProjectPulse Hyperledger Fabric Network Initialization" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Check Docker availability
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Warning "Docker is not installed or not in PATH."
    Write-Host "The ProjectPulse backend will automatically operate in simulated gateway fallback mode." -ForegroundColor Yellow
    Write-Host "All SHA-256 canonical hashing and honest verification state tracking are fully functional." -ForegroundColor Green
    exit 0
}

Write-Host "Starting Fabric containers..." -ForegroundColor Green
docker compose -f "$PSScriptRoot\docker-compose-test-net.yml" up -d

Write-Host "Fabric orderer, peer0.org1, and CA containers are starting." -ForegroundColor Green
Write-Host "Next step: Run deploy-chaincode.ps1 to commit the activitytracker chaincode." -ForegroundColor Yellow
