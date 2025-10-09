#!/bin/sh
set -e

# Find first jar in /app
JAR_FILE="$(ls /app/*.jar 2>/dev/null | head -n1)"
if [ -z "$JAR_FILE" ]; then
  echo "No JAR found in /app"
  exit 1
fi

# Default SERVER_PORT if not set
SERVER_PORT=${SERVER_PORT:-8080}

echo "Starting $JAR_FILE on port $SERVER_PORT"
exec java ${JAVA_OPTS} -jar "$JAR_FILE" --server.port=${SERVER_PORT}

