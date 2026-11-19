@echo off
setlocal
cd /d "%~dp0\.."

echo ===================================================
echo  Starting ProjectPulse Full-Stack Environment
echo ===================================================
echo  Launching Spring Boot Backend...
start "ProjectPulse Backend (Port 8080)" cmd /k "scripts\start-backend.bat"

echo  Waiting 6 seconds for backend initialization...
timeout /t 6 /nobreak >nul

echo  Launching Vite React Frontend...
start "ProjectPulse Frontend (Port 5173)" cmd /k "scripts\start-frontend.bat"

echo  Launching Desktop Tracking Agent...
start "ProjectPulse Desktop Agent" cmd /k "scripts\start-desktop-agent.bat"

echo ===================================================
echo  All services launched in separate windows!
echo  - Frontend: http://localhost:5173
echo  - Backend API: http://localhost:8080
echo  - Swagger UI: http://localhost:8080/swagger-ui.html
echo  - H2 Console: http://localhost:8080/h2-console
echo ===================================================
pause

