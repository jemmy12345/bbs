@echo off
setlocal
set APP_NAME=bbs-admin.jar
set JAVA_OPTS=-Dname=%APP_NAME% -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
echo Usage: ry.bat {start^|stop^|restart^|status}
exit /b 1

:start
start "bbs-admin" javaw %JAVA_OPTS% -jar "%APP_NAME%"
echo Started %APP_NAME%.
exit /b 0

:stop
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":9082" ^| findstr "LISTENING"') do taskkill /f /pid %%a
echo Stopped service on port 9082 if it was running.
exit /b 0

:restart
call :stop
call :start
exit /b 0

:status
netstat -ano | findstr ":9082"
exit /b 0
