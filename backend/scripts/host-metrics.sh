#!/usr/bin/env bash
# F22: Host resource monitor — alerts via Server酱 when disk > 80% or free RAM < 500 MB.
# Crontab: */10 * * * * /opt/careerloop/scripts/host-metrics.sh >> /var/log/careerloop-metrics.log 2>&1
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../.env.prod" 2>/dev/null || true

SEND_KEY="${SERVER_CHAN_SEND_KEY:-}"

DISK_USED=$(df / | awk 'NR==2 {gsub("%",""); print $5}')
MEM_FREE_MB=$(free -m | awk '/^Mem:/ {print $7}')

ALERTS=()

if [ "${DISK_USED}" -ge 80 ]; then
  ALERTS+=("🚨 磁盘使用率 ${DISK_USED}% (阈值 80%)")
fi

if [ "${MEM_FREE_MB}" -lt 500 ]; then
  ALERTS+=("🚨 可用内存 ${MEM_FREE_MB}MB (阈值 500MB)")
fi

if [ "${#ALERTS[@]}" -gt 0 ] && [ -n "${SEND_KEY}" ]; then
  MSG=$(printf '%s\n' "${ALERTS[@]}")
  curl -s "https://sctapi.ftqq.com/${SEND_KEY}.send" \
    -d "title=[CareerLoop] 主机资源告警" \
    -d "desp=${MSG}  时间: $(date '+%F %T')" \
    > /dev/null
  echo "[metrics] alert sent: ${MSG}"
fi
