#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

COMPOSE="docker-compose"

# Locate Java 17 — required for Maven build
find_java17() {
  if [ -n "${JAVA_HOME:-}" ] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17'; then
    echo "$JAVA_HOME"
    return
  fi
  # macOS: use java_home utility
  if command -v /usr/libexec/java_home &>/dev/null; then
    /usr/libexec/java_home -v 17 2>/dev/null || true
  fi
}

usage() {
  cat <<EOF
Usage: ./run.sh [COMMAND]

Commands:
  up      Build jar and start all services (default)
  down    Stop and remove containers
  logs    Follow application logs
  test    Run unit tests
  clean   Remove containers and volumes

EOF
}

case "${1:-up}" in
  up)
    JAVA17="$(find_java17)"
    if [ -z "$JAVA17" ]; then
      echo "Error: Java 17 is required. Install it from https://adoptium.net"
      exit 1
    fi
    export JAVA_HOME="$JAVA17"

    echo "Building application..."
    mvn -q -DskipTests package

    echo "Starting services..."
    $COMPOSE up --build -d

    echo ""
    echo "Waiting for the app to be ready..."
    for i in $(seq 1 30); do
      if curl -sf http://localhost:8080/api-docs > /dev/null 2>&1; then
        echo "App is ready!"
        echo ""
        echo "  API base:  http://localhost:8080"
        echo "  Swagger:   http://localhost:8080/swagger-ui.html"
        echo "  OpenAPI:   http://localhost:8080/api-docs"
        exit 0
      fi
      printf "."
      sleep 2
    done
    echo ""
    echo "App did not respond in time. Check logs with: ./run.sh logs"
    exit 1
    ;;
  down)
    $COMPOSE down
    ;;
  logs)
    $COMPOSE logs -f app
    ;;
  test)
    JAVA17="$(find_java17)"
    if [ -z "$JAVA17" ]; then
      echo "Error: Java 17 is required to run tests."
      exit 1
    fi
    export JAVA_HOME="$JAVA17"
    echo "Running tests..."
    mvn test
    ;;
  clean)
    $COMPOSE down -v
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    echo "Unknown command: ${1}"
    usage
    exit 1
    ;;
esac
