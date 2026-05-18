@echo off

call .venv\Scripts\activate

echo Starting Kafka...
start "Kafka Check" cmd /k "docker ps"

echo Starting Model Server...
start "Model Server" cmd /k "cd /d %~dp0 && call .venv\Scripts\activate && python model_server.py"

timeout /t 3

echo Starting UI Server...
start "UI Server" cmd /k "cd /d %~dp0 && call .venv\Scripts\activate && python ui_server.py"

timeout /t 3

echo Starting Streams Processor...
start "Streams Processor" cmd /k "cd /d %~dp0 && mvn exec:java "-Dexec.mainClass=com.assignment.StreamsProcessor""

timeout /t 5

echo Starting Output Consumer...
start "Output Consumer" cmd /k "cd /d %~dp0 && mvn exec:java "-Dexec.mainClass=com.assignment.OutputConsumer""

timeout /t 3

echo Starting Producer...
start "Producer" cmd /k "cd /d %~dp0 && mvn exec:java "-Dexec.mainClass=com.assignment.Producer""

echo All components started. Opening browser...
timeout /t 5
start http://localhost:5002