#!/usr/bin/env bash
# F23: One-command rollback to the previous backend image.
# Usage: ./scripts/rollback.sh
set -euo pipefail

# Optional: source local .env.deploy at the repo root (see deploy-backend.sh).
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
if [ -f "${REPO_ROOT}/.env.deploy" ]; then
  # shellcheck disable=SC1091
  set -a; . "${REPO_ROOT}/.env.deploy"; set +a
fi

SERVER_USER="${DEPLOY_USER:?DEPLOY_USER must be set}"
SERVER_HOST="${DEPLOY_HOST:?DEPLOY_HOST must be set}"
SERVER_DIR="${DEPLOY_DIR:?DEPLOY_DIR must be set}"

echo "========================================"
echo " CareerLoop Backend ROLLBACK"
echo " Target: ${SERVER_USER}@${SERVER_HOST}"
echo "========================================"
echo ""
read -rp "Rolling back to :rollback image. Confirm? (yes/no): " CONFIRM
if [ "${CONFIRM}" != "yes" ]; then
  echo "Aborted."
  exit 0
fi

ssh "${SERVER_USER}@${SERVER_HOST}" "
  cd ${SERVER_DIR}/backend
  echo '[rollback] Switching :rollback → :latest'
  docker tag careerloop-backend:rollback careerloop-backend:latest
  echo '[rollback] Restarting backend container'
  docker compose up -d --no-deps --force-recreate app
  sleep 8
  STATUS=\$(docker inspect --format='{{.State.Health.Status}}' careerloop-backend 2>/dev/null || echo 'unknown')
  echo \"[rollback] Container status: \${STATUS}\"
"

echo ""
echo "✅ Rollback applied. Monitor with:"
echo "   ssh ${SERVER_USER}@${SERVER_HOST} 'docker logs careerloop-backend --tail 50 -f'"
