@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"
title Render4U - Launching...

echo ========================================================
echo               RENDER4U - LAUNCHER
echo ========================================================
echo.

:: Detect Java installation
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        goto :java_found
    )
)

:: Check known IntelliJ JBR location
if exist "D:\Programs\IntelliJ IDEA 2026.2.0.1\jbr\bin\java.exe" (
    set "JAVA_HOME=D:\Programs\IntelliJ IDEA 2026.2.0.1\jbr"
    goto :java_found
)

:: Search common installation paths
for %%D in (
    "C:\Program Files\Java\jdk-25*"
    "C:\Program Files\Eclipse Adoptium\jdk-25*"
    "C:\Program Files\Microsoft\jdk-25*"
    "C:\Program Files\BellSoft\LibericaJDK-25*"
    "D:\Programs\Java\jdk-25*"
) do (
    if exist "%%~fD\bin\java.exe" (
        set "JAVA_HOME=%%~fD"
        goto :java_found
    )
)

:: Fallback check if java is already in PATH
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
    goto :run_gradle
)

echo [ERROR] Java 25 was not found!
echo Please ensure JAVA_HOME is set or Java 25 is installed.
echo.
pause
exit /b 1

:java_found
echo [*] Using Java from: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

:run_gradle
echo [*] Starting Minecraft Client...
echo.

call ".\gradlew.bat" runClient %*

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Minecraft Client exited with code %ERRORLEVEL%.
    pause
)
