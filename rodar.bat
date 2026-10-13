@echo off
cd /d "%~dp0"
call "%~dp0mvnw.cmd" -q javafx:run
if errorlevel 1 pause
