# ProjectPulse Frontend Launcher (PowerShell)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ScriptDir "..\frontend")

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Starting ProjectPulse Frontend (Vite + React)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Dashboard: http://localhost:5173" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan

if (-not (Test-Path "node_modules")) {
    Write-Host "[INFO] Installing frontend dependencies..." -ForegroundColor Yellow
    npm.cmd install
}

npm.cmd run dev

