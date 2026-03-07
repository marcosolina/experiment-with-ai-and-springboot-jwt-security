#!/usr/bin/env bash
set -e

REPO="https://github.com/marcosolina/experiment-with-ai-and-springboot-jwt-security.git"
FOLDER="experiment-with-ai-and-springboot-jwt-security"

echo ""
echo "========================================="
echo "  POC Security - JWT Auth Demo"
echo "========================================="
echo ""

# Check prerequisites
missing=""
command -v git >/dev/null 2>&1 || missing="$missing git"
command -v docker >/dev/null 2>&1 || missing="$missing docker"

if [ -n "$missing" ]; then
    echo "Missing required tools:$missing"
    echo "Please install them and try again."
    exit 1
fi

# Check Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "[1/3] Cloning repository..."

if [ -d "$FOLDER" ]; then
    echo "  Folder '$FOLDER' already exists. Pulling latest changes..."
    cd "$FOLDER"
    git pull --quiet
else
    git clone --quiet "$REPO"
    cd "$FOLDER"
fi

echo "[2/3] Building and starting services with Docker..."
echo "  This may take a few minutes on the first run."
echo ""

docker compose up --build -d

echo ""
echo "[3/3] Services are starting..."
echo ""
echo "  Waiting for tests to complete. Follow the logs with:"
echo "    docker compose logs -f e2e-tests"
echo ""
echo "========================================="
echo "  Test report: http://localhost:9323"
echo "  Frontend:    http://localhost:5173"
echo "  Supervisor:  http://localhost:8081"
echo "  Messages:    http://localhost:8082"
echo "========================================="
echo ""
echo "  Login with: admin / admin123"
echo ""
echo "  To stop:  docker compose down"
echo "  To logs:  docker compose logs -f"
echo ""

# Open browser
if command -v xdg-open >/dev/null 2>&1; then
    xdg-open "http://localhost:5173" 2>/dev/null &
    xdg-open "http://localhost:9323" 2>/dev/null &
elif command -v open >/dev/null 2>&1; then
    open "http://localhost:5173"
    open "http://localhost:9323"
fi
