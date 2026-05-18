@echo off
set BASE=%~dp0

echo =====================================
echo Starting Real-Time Streaming Project
echo =====================================

echo.
echo [1/6] Starting Docker (Kafka + Zookeeper)...

REM Start Docker Desktop if not running
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"

timeout /t 10 >nul

REM Start Kafka stack
start "KAFKA_DOCKER" cmd /k "title KAFKA_DOCKER && cd /d %BASE% && docker compose up"

timeout /t 10 >nul

echo.
echo [2/6] Starting Model Server...
start "MODEL_SERVER" cmd /k "title MODEL_SERVER && cd /d %BASE% && call .venv\Scripts\activate && python model_server.py"

timeout /t 3 >nul

echo.
echo [3/6] Starting UI Server...
start "UI_SERVER" cmd /k "title UI_SERVER && cd /d %BASE% && call .venv\Scripts\activate && python ui_server.py"

timeout /t 3 >nul

echo.
echo [4/6] Starting Streams Processor...
start "STREAMS_PROCESSOR" cmd /k "title STREAMS_PROCESSOR && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.StreamsProcessor"

timeout /t 5 >nul

echo.
echo [5/6] Starting Output Consumer...
start "OUTPUT_CONSUMER" cmd /k "title OUTPUT_CONSUMER && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.OutputConsumer"

timeout /t 3 >nul

echo.
echo [6/6] Starting Producer...
start "PRODUCER" cmd /k "title PRODUCER && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.Producer"

echo.
echo =====================================
echo All services started successfully!
echo Opening UI...
echo =====================================

timeout /t 5 >nul
start http://localhost:5002