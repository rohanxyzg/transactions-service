#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

COMPOSE="docker-compose"

usage() {
  cat <<EOF
Usage: ./run.sh [COMMAND]

Commands:
  up      Build and start all services with Docker Compose (default)
  down    Stop and remove containers
  logs    Follow application logs
  test    Run unit tests (requires Java 17 + Maven locally)
  clean   Remove containers and volumes

EOF
}

case "${1:-up}" in
  up)
    echo "Building and starting Transactions Service..."
    $COMPOSE up --build -d
    echo ""
    echo "Services started. Waiting for the app to be ready..."
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
    ;;
  down)
    $COMPOSE down
    ;;
  logs)
    $COMPOSE logs -f app
    ;;
  test)
    echo "Running unit tests..."
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
