# ProjectPulse Desktop Agent Launcher (PowerShell)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ScriptDir "..")

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Starting ProjectPulse Desktop Tracking Agent (Swing GUI)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$jarPath = "desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[INFO] Building desktop-agent JAR..." -ForegroundColor Yellow
    & ".\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd" -f desktop-agent\pom.xml clean package -DskipTests
}

java -jar $jarPath $args

