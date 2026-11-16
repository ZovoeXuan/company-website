@echo off
REM One-click dev startup. Double-click this file to run the three services in
REM their own minimized windows. They keep running after the WorkBuddy session
REM ends, because they belong to your desktop session, not to the agent.
setlocal
set "ROOT=%~dp0"

echo Starting backend on 8088 ...
start "CMS Server 8088" /D "%ROOT%server" /MIN java -jar target\company-website-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=local --server.port=8088

echo Starting website on 5173 ...
start "CMS Web 5173" /D "%ROOT%web" /MIN node node_modules\vite\bin\vite.js

echo Starting admin on 5174 ...
start "CMS Admin 5174" /D "%ROOT%admin" /MIN node node_modules\vite\bin\vite.js

echo.
echo All three services launched in minimized windows.
echo   Website : http://localhost:5173/
echo   Admin   : http://localhost:5174/admin/
echo   API     : http://localhost:8088/actuator/health
echo.
echo Close the three minimized windows to stop the services.
echo Backend source changes require: cd server ^&^& mvnw.cmd -q package -DskipTests
timeout /t 6 >nul
