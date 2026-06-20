#!/usr/bin/env bash
# Staging deployment script for Kanban Board
# Usage: STAGE_HOST=user@host ./deploy/staging.sh

set -euo pipefail

HOST="${STAGE_HOST:?Set STAGE_HOST=user@host}"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.production"
PROJECT_DIR="/home/deploy/kanban"

echo "=== Building images ==="
./gradlew :spring:bootJar --no-daemon

(cd vue && npm ci && npm run build)

echo "=== Copying project to staging ==="
rsync -avz --delete \
  --exclude='node_modules' \
  --exclude='.git' \
  --exclude='build' \
  --exclude='vue/node_modules' \
  --exclude='vue/dist' \
  --exclude='e2etest/node_modules' \
  --exclude='e2etest/report' \
  ./ "$HOST:$PROJECT_DIR/"

echo "=== Deploying on staging ==="
ssh "$HOST" bash -s << "REMOTE"
  set -euo pipefail
  cd "$PROJECT_DIR"

  # Create .env from example if it doesn't exist
  if [ ! -f "$ENV_FILE" ]; then
    if [ -f ".env.example" ]; then
      cp .env.example "$ENV_FILE"
      echo "Created $ENV_FILE from .env.example — please review secrets before production!"
    else
      echo "ERROR: No .env.production and no .env.example found" >&2
      exit 1
    fi
  fi

  # Load environment
  set -a; source "$ENV_FILE"; set +a

  # Pull base images & build app images
  docker compose -f "$COMPOSE_FILE" pull postgres minio
  docker compose -f "$COMPOSE_FILE" build

  # Start stack
  docker compose -f "$COMPOSE_FILE" up -d --wait --wait-timeout 120

  # Healthcheck
  echo "=== Healthcheck ==="
  for i in $(seq 1 12); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/actuator/health 2>/dev/null || echo "000")
    if [ "$STATUS" = "200" ]; then
      echo "API is healthy (attempt $i)"
      break
    fi
    echo "Waiting for API... (attempt $i)"
    sleep 5
  done

  # Verify all services
  docker compose -f "$COMPOSE_FILE" ps
REMOTE

echo "=== Running E2E tests against staging ==="
cd e2etest
npm ci
npx playwright install --with-deps chromium
BASE_URL="http://$HOST" npx playwright test
cd ..

echo "=== Done ==="
