# ProjectPulse Full-Stack Launcher (PowerShell)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ScriptDir "..")

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Starting ProjectPulse Full-Stack Environment" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

Write-Host "Launching Spring Boot Backend..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "scripts\start-backend.ps1"

Write-Host "Waiting 6 seconds for backend initialization..." -ForegroundColor Yellow
Start-Sleep -Seconds 6

Write-Host "Launching Vite React Frontend..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "scripts\start-frontend.ps1"

Write-Host "Launching Desktop Tracking Agent..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "scripts\start-desktop-agent.ps1"

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " All services launched in separate windows!" -ForegroundColor Green
Write-Host " - Frontend:    http://localhost:5173" -ForegroundColor Cyan
Write-Host " - Backend API: http://localhost:8080" -ForegroundColor Cyan
Write-Host " - Swagger UI:  http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host " - H2 Console:  http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

