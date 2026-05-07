#!/bin/sh
# Maven wrapper script for Unix-like systems
BASE_DIR=$(dirname "$0")
if [ -z "$MVNW_VERBOSE" ]; then
  MVNW_VERBOSE=false
fi

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXEC="java"
else
  JAVA_EXEC="$JAVA_HOME/bin/java"
fi

WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Missing maven-wrapper.jar in $WRAPPER_DIR"
  exit 1
fi

exec "$JAVA_EXEC" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

