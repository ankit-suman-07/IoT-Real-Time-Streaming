@echo off
set BASE=%~dp0

echo =====================================
echo Starting Real-Time Streaming Project
echo Assumes Kafka is already running!
echo =====================================

echo.
echo [1/4] Starting Model Server...
start "MODEL_SERVER" cmd /k "title MODEL_SERVER && cd /d %BASE% && call .venv\Scripts\activate && python model_server.py"
timeout /t 5 >nul

echo.
echo [2/4] Starting UI Server...
start "UI_SERVER" cmd /k "title UI_SERVER && cd /d %BASE% && call .venv\Scripts\activate && python ui_server.py"
timeout /t 5 >nul

echo.
echo [3/4] Starting Streams Processor...
start "STREAMS_PROCESSOR" cmd /k "title STREAMS_PROCESSOR && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.StreamsProcessor"
timeout /t 20 >nul

echo.
echo [4/4] Starting Output Consumer and Producer...
start "OUTPUT_CONSUMER" cmd /k "title OUTPUT_CONSUMER && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.OutputConsumer"
timeout /t 5 >nul

start "PRODUCER" cmd /k "title PRODUCER && cd /d %BASE% && mvn exec:java -Dexec.mainClass=com.assignment.Producer"

echo.
echo =====================================
echo All services started!
echo Opening UI...
echo =====================================
timeout /t 8 >nul
start http://localhost:5002