@echo off
setlocal
cd /d "%~dp0\..\frontend"
echo ===================================================
echo  Starting ProjectPulse Frontend (Vite + React)
echo ===================================================
echo  Dashboard: http://localhost:5173
echo ===================================================

if not exist "node_modules" (
    echo [INFO] Installing frontend dependencies...
    call npm.cmd install
)

call npm.cmd run dev
pause

