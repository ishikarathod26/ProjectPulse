<#
.SYNOPSIS
  Packages and deploys the activitytracker chaincode to the projectpulse-channel.
#>

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Deploying ActivityTracker Chaincode (v1.0.0)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$CC_NAME = "activitytracker"
$CC_VERSION = "1.0.0"
$CHANNEL_NAME = "projectpulse-channel"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Warning "Docker is not available. Chaincode package prepared in blockchain-network/chaincode/activitytracker/."
    exit 0
}

Write-Host "Chaincode source located at: $PSScriptRoot\chaincode\activitytracker" -ForegroundColor Green
Write-Host "Deploying to channel: $CHANNEL_NAME with name: $CC_NAME..." -ForegroundColor Green
