#!/usr/bin/env bash
# F22: Restore MySQL from a backup on Aliyun OSS.
# Usage: ./restore-from-backup.sh [backup-filename]
# Example: ./restore-from-backup.sh careerloop_20260502_020001.sql.gz
# If no filename is given, lists available backups and prompts.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env.prod" 2>/dev/null || true

DB_NAME="${MYSQL_DB:-career_db}"
DB_USER="root"
DB_PASS="${MYSQL_ROOT_PASSWORD:-}"

OSS_BUCKET="${OSS_BUCKET_NAME:-careerloop-prod}"
OSS_ENDPOINT="${OSS_ENDPOINT:-oss-cn-chengdu.aliyuncs.com}"
OSS_KEY_ID="${OSS_ACCESS_KEY_ID:-}"
OSS_KEY_SECRET="${OSS_ACCESS_KEY_SECRET:-}"

BACKUP_NAME="${1:-}"

echo "========================================"
echo " CareerLoop MySQL Restore"
echo "========================================"
echo ""

if [ -z "${BACKUP_NAME}" ]; then
  echo "Available backups:"
  ossutil ls "oss://${OSS_BUCKET}/backups/mysql/" \
    --endpoint "${OSS_ENDPOINT}" \
    --access-key-id "${OSS_KEY_ID}" \
    --access-key-secret "${OSS_KEY_SECRET}" | grep ".sql.gz" | awk '{print $NF}'
  echo ""
  read -rp "Enter backup filename (e.g. careerloop_20260502_020001.sql.gz): " BACKUP_NAME
fi

LOCAL_DUMP="/tmp/${BACKUP_NAME}"
OSS_PATH="oss://${OSS_BUCKET}/backups/mysql/${BACKUP_NAME}"

echo "[restore] Downloading ${BACKUP_NAME} from OSS..."
ossutil cp "${OSS_PATH}" "${LOCAL_DUMP}" \
  --endpoint "${OSS_ENDPOINT}" \
  --access-key-id "${OSS_KEY_ID}" \
  --access-key-secret "${OSS_KEY_SECRET}"

echo "[restore] Download complete. Starting restore into database '${DB_NAME}'..."
echo ""
echo "⚠️  WARNING: This will overwrite all data in '${DB_NAME}'."
read -rp "Type 'YES' to confirm: " CONFIRM
if [ "${CONFIRM}" != "YES" ]; then
  echo "Aborted."
  exit 1
fi

gunzip -c "${LOCAL_DUMP}" | docker exec -i careerloop-mysql \
  mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}"

echo ""
echo "[restore] ✅ Restore complete from ${BACKUP_NAME}"
echo "[restore] Verifying row counts..."
docker exec careerloop-mysql mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" \
  -e "SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema='${DB_NAME}' ORDER BY table_rows DESC LIMIT 15;"

rm -f "${LOCAL_DUMP}"
echo "[restore] Done."
