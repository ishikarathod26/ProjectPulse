@echo off
setlocal
cd /d "%~dp0\.."
echo ===================================================
echo  Starting ProjectPulse Desktop Tracking Agent (Swing GUI)
echo ===================================================

if not exist "desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar" (
    echo [ERROR] Agent JAR not found. Building now...
    call scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f desktop-agent\pom.xml clean package -DskipTests
)

java -jar desktop-agent\target\projectpulse-desktop-agent-1.0.0.jar %*
pause

