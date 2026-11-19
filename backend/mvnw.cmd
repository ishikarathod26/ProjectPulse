@echo off
setlocal
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Java\jdk-21.0.12.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
    )
)
set "LOCAL_MVN=%~dp0..\scripts\tools\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%LOCAL_MVN%" (
    call "%LOCAL_MVN%" %*
) else (
    mvn %*
)
exit /b %ERRORLEVEL%
