#!/usr/bin/env bash
# F23: Blue-green backend deploy script.
# Usage (run from local machine after `mvn package -DskipTests`):
#   ./scripts/deploy-backend.sh
#
# What it does:
#   1. Copies the built JAR to the server
#   2. Tags the current image as :rollback (one-version safety net)
#   3. Rebuilds the Docker image with the new JAR
#   4. Brings the backend container up; waits for health check
#   5. Auto-rolls back if health check fails within 60s
set -euo pipefail

SERVER_USER="${DEPLOY_USER:-ubuntu}"
SERVER_HOST="${DEPLOY_HOST:-43.138.240.228}"
SERVER_DIR="${DEPLOY_DIR:-/opt/careerloop/backend}"
LOCAL_JAR="$(find target -name '*.jar' -not -name '*sources*' | head -1)"

if [ -z "${LOCAL_JAR}" ]; then
  echo "❌ No JAR found in target/. Run: mvn package -DskipTests"
  exit 1
fi

echo "========================================"
echo " CareerLoop Backend Deploy"
echo " JAR: ${LOCAL_JAR}"
echo " Target: ${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}"
echo "========================================"

echo ""
echo "[1/5] Uploading JAR to server..."
scp "${LOCAL_JAR}" "${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/app.jar"

echo "[2/5] Tagging current image as rollback..."
ssh "${SERVER_USER}@${SERVER_HOST}" "
  cd ${SERVER_DIR}
  docker tag careerloop-backend:latest careerloop-backend:rollback 2>/dev/null || true
  echo '  Tagged :latest → :rollback'
"

echo "[3/5] Rebuilding Docker image..."
ssh "${SERVER_USER}@${SERVER_HOST}" "
  cd ${SERVER_DIR}
  docker compose build app
"

echo "[4/5] Deploying new container..."
ssh "${SERVER_USER}@${SERVER_HOST}" "
  cd ${SERVER_DIR}
  docker compose up -d --no-deps app
"

echo "[5/5] Waiting for health check (up to 90s)..."
HEALTHY=false
for i in $(seq 1 18); do
  sleep 5
  STATUS=$(ssh "${SERVER_USER}@${SERVER_HOST}" "
    docker inspect --format='{{.State.Health.Status}}' careerloop-backend 2>/dev/null || echo 'starting'
  ")
  echo "  [${i}] Health: ${STATUS}"
  if [ "${STATUS}" = "healthy" ]; then
    HEALTHY=true
    break
  fi
done

if [ "${HEALTHY}" = "true" ]; then
  echo ""
  echo "✅ Deploy successful! Backend is healthy."
else
  echo ""
  echo "❌ Health check failed — auto-rolling back..."
  ssh "${SERVER_USER}@${SERVER_HOST}" "
    cd ${SERVER_DIR}
    docker tag careerloop-backend:rollback careerloop-backend:latest
    docker compose up -d --no-deps --force-recreate app
    echo 'Rollback applied'
  "
  echo "🔄 Rollback complete. Please check logs: ssh ${SERVER_USER}@${SERVER_HOST} 'docker logs careerloop-backend --tail 100'"
  exit 1
fi
