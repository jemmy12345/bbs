@echo off
setlocal
set PROJECT_ROOT=%~dp0..
set JAR_DIR=%PROJECT_ROOT%\backend\bbs-admin\target
set APP_NAME=bbs-admin.jar
set JAVA_EXE=D:\LenovoSoftstore\Install\Eclipse Adoptium\jdk-17\bin\java.exe
set JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m
cd /d "%JAR_DIR%"
if exist "%JAVA_EXE%" (
  "%JAVA_EXE%" %JAVA_OPTS% -jar "%APP_NAME%"
) else (
  java %JAVA_OPTS% -jar "%APP_NAME%"
)
endlocal
pause
