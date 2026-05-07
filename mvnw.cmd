@ECHO OFF
REM Maven wrapper script for Windows

SETLOCAL
SET "BASE_DIR=%~dp0"
SET "BASE_DIR=%BASE_DIR:~0,-1%"

IF "%MVNW_VERBOSE%"=="" SET "MVNW_VERBOSE=false"

IF "%JAVA_HOME%"=="" (
  SET "JAVA_EXEC=java"
) ELSE (
  SET "JAVA_EXEC=%JAVA_HOME%\bin\java"
)

SET "WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper"
SET "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Missing maven-wrapper.jar in %WRAPPER_DIR%
  EXIT /B 1
)

"%JAVA_EXEC%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL

