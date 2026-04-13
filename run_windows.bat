@echo off
REM ─────────────────────────────────────────────────────
REM run_windows.bat — Compile & run on Windows
REM Requires: JDK 17+
REM JavaFX:   lib\   (bundled — Windows x64 native libraries)
REM ─────────────────────────────────────────────────────

set JAVAFX_PATH=lib
set MODULES=javafx.controls,javafx.fxml

echo ╔══════════════════════════════════════════╗
echo ║   Civilian Complaint Portal — Windows    ║
echo ╚══════════════════════════════════════════╝

REM Step 1: Compile
echo [1/2] Compiling...
if exist out rmdir /s /q out
javac --module-path %JAVAFX_PATH% --add-modules %MODULES% -d out ^
    Main.java ^
    enums\*.java ^
    exceptions\*.java ^
    profile\*.java ^
    users\*.java ^
    complaints\*.java ^
    containers\*.java ^
    store\*.java ^
    priority\*.java ^
    search\*.java ^
    threads\*.java ^
    gui\*.java
echo       Compilation successful.

REM Step 2: Run
if "%1"=="--test" (
    echo [2/2] Running backend tests...
    java --module-path %JAVAFX_PATH% --add-modules %MODULES% -cp out Main --test
) else (
    echo [2/2] Launching GUI...
    java --module-path %JAVAFX_PATH% --add-modules %MODULES% -cp out Main %*
)

pause
