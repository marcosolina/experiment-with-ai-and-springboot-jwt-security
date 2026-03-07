#!/bin/bash
set -e

echo "Waiting for services to be ready..."

until curl -sf http://supervisor:8081/api/auth/.well-known/jwks.json > /dev/null 2>&1; do
  echo "  Waiting for supervisor..."
  sleep 2
done
echo "Supervisor is ready"

until curl -sf http://messages:8082/api/messages/health > /dev/null 2>&1; do
  echo "  Waiting for messages..."
  sleep 2
done
echo "Messages is ready"

until curl -sf http://frontend:80 > /dev/null 2>&1; do
  echo "  Waiting for frontend..."
  sleep 2
done
echo "Frontend is ready"

echo ""
echo "Running Playwright tests..."
echo ""

npx playwright test || true

echo ""
echo "========================================="
echo "  Test report: http://localhost:9323"
echo "========================================="
echo ""

npx serve playwright-report -l 9323
