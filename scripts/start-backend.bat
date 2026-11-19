@echo off
setlocal
cd /d "%~dp0\.."
echo ===================================================
echo  Starting ProjectPulse Backend (Spring Boot 3.3.4)
echo ===================================================
echo  API Base URL: http://localhost:8080
echo  Swagger UI:   http://localhost:8080/swagger-ui.html
echo  H2 Console:   http://localhost:8080/h2-console
echo ===================================================

if not exist "backend\target\projectpulse-backend-1.0.0.jar" (
    echo [ERROR] Backend JAR not found. Building now...
    call scripts\tools\apache-maven-3.9.6\bin\mvn.cmd -f backend\pom.xml clean package -DskipTests
)

java -jar backend\target\projectpulse-backend-1.0.0.jar --spring.profiles.active=dev
pause

