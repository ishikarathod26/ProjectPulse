# ProjectPulse Backend Launcher (PowerShell)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ScriptDir "..")

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Starting ProjectPulse Backend (Spring Boot 3.3.4)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " API Base URL: http://localhost:8080" -ForegroundColor Green
Write-Host " Swagger UI:   http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host " H2 Console:   http://localhost:8080/h2-console" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan

$jarPath = "backend\target\projectpulse-backend-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[INFO] Building backend JAR..." -ForegroundColor Yellow
    & ".\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd" -f backend\pom.xml clean package -DskipTests
}

java -jar $jarPath --spring.profiles.active=dev

