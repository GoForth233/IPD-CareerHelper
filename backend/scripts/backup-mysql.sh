#!/usr/bin/env bash
# F22: Daily MySQL backup → Aliyun OSS
# Crontab: 0 2 * * * /opt/careerloop/backend/scripts/backup-mysql.sh >> /var/log/careerloop-backup.log 2>&1
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env.prod" 2>/dev/null || true

DB_HOST="${MYSQL_HOST:-127.0.0.1}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_NAME="${MYSQL_DB:-career_db}"
DB_USER="root"
DB_PASS="${MYSQL_ROOT_PASSWORD:-}"

OSS_BUCKET="${OSS_BUCKET_NAME:-careerloop-prod}"
OSS_ENDPOINT="${OSS_ENDPOINT:-oss-cn-chengdu.aliyuncs.com}"
OSS_KEY_ID="${OSS_ACCESS_KEY_ID:-}"
OSS_KEY_SECRET="${OSS_ACCESS_KEY_SECRET:-}"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="/tmp/careerloop_${TIMESTAMP}.sql.gz"
OSS_PATH="oss://${OSS_BUCKET}/backups/mysql/careerloop_${TIMESTAMP}.sql.gz"

echo "[backup] $(date '+%F %T') — starting MySQL backup"

# Dump from the running careerloop-mysql container
docker exec careerloop-mysql \
  mysqldump -u"${DB_USER}" -p"${DB_PASS}" \
  --single-transaction --quick --hex-blob \
  "${DB_NAME}" | gzip > "${BACKUP_FILE}"

DUMP_SIZE=$(du -sh "${BACKUP_FILE}" | cut -f1)
echo "[backup] dump complete: ${DUMP_SIZE}"

# Upload to OSS using ossutil (must be installed: https://help.aliyun.com/document_detail/120075.html)
if command -v ossutil &>/dev/null; then
  ossutil cp "${BACKUP_FILE}" "${OSS_PATH}" \
    --endpoint "${OSS_ENDPOINT}" \
    --access-key-id "${OSS_KEY_ID}" \
    --access-key-secret "${OSS_KEY_SECRET}"
  echo "[backup] uploaded to ${OSS_PATH}"
else
  echo "[backup] WARNING: ossutil not found, backup kept locally at ${BACKUP_FILE}"
fi

# Keep only last 7 local temp files
find /tmp -name "careerloop_*.sql.gz" -mtime +1 -delete 2>/dev/null || true

# Server酱 success alert (optional, comment out if noisy)
if [ -n "${SERVER_CHAN_SEND_KEY:-}" ]; then
  curl -s "https://sctapi.ftqq.com/${SERVER_CHAN_SEND_KEY}.send" \
    -d "title=[CareerLoop] 备份成功" \
    -d "desp=时间: $(date '+%F %T')  大小: ${DUMP_SIZE}  路径: backups/mysql/careerloop_${TIMESTAMP}.sql.gz" \
    > /dev/null
fi

echo "[backup] $(date '+%F %T') — done"
