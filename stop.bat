@echo off

echo ================================
echo STOPPING FULL SYSTEM
echo ================================

echo.
echo Stopping Docker (Kafka + Zookeeper)...
cd /d %~dp0
docker compose down

echo.
echo Killing project terminals by process...

taskkill /F /FI "WINDOWTITLE eq MODEL_SERVER*" /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq UI_SERVER*" /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq STREAMS_PROCESSOR*" /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq OUTPUT_CONSUMER*" /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq PRODUCER*" /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq KAFKA_DOCKER*" /T >nul 2>&1

echo.
echo Backup kill (if anything still running)...
taskkill /F /IM python.exe >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1

echo.
echo SYSTEM STOPPED
pause