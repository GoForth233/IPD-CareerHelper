#!/usr/bin/env bash
# =============================================================
# CareerLoop — Flyway checksum 修复脚本（核弹选项）
#
# 用途：当 repair-on-migrate 配置重启仍失败时，手动清理
#       flyway_schema_history 中失败的 V7 记录，让 Flyway
#       在下次启动时重新执行 V7 的干净版本。
#
# 在服务器 backend/ 目录下运行：
#   bash scripts/fix-flyway-repair.sh
# =============================================================
set -euo pipefail

MYSQL_CONTAINER="careerloop-mysql"
BACKEND_CONTAINER="careerloop-backend"

if [ ! -f .env.prod ]; then
    echo "[✗] 找不到 .env.prod，请先创建"
    exit 1
fi

ROOT_PWD=$(grep "^MYSQL_ROOT_PASSWORD=" .env.prod | cut -d= -f2-)

echo "── 1. 停止后端容器 ──────────────────────────────────"
docker stop "${BACKEND_CONTAINER}" 2>/dev/null || true

echo ""
echo "── 2. 查看 Flyway 历史 ─────────────────────────────"
docker exec "${MYSQL_CONTAINER}" \
    mysql -uroot -p"${ROOT_PWD}" career_db \
    -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

echo ""
echo "── 3. 删除失败的 V7 迁移记录 ───────────────────────"
DELETED=$(docker exec "${MYSQL_CONTAINER}" \
    mysql -uroot -p"${ROOT_PWD}" career_db \
    -e "DELETE FROM flyway_schema_history WHERE version='7' AND success=0; SELECT ROW_COUNT() as deleted;" \
    2>&1 | grep -v "^deleted" | tail -1 || echo "0")
echo "  删除了 ${DELETED} 条失败记录"

echo ""
echo "── 4. 重新启动后端（Flyway 将重新执行 V7）─────────"
docker start "${BACKEND_CONTAINER}"
echo "  等待 60 秒让 Spring Boot 启动..."
sleep 60

echo ""
echo "── 5. 检查健康状态 ─────────────────────────────────"
curl -sf http://localhost:8080/actuator/health && echo "" && \
    echo "  [✓] 后端启动成功！" || \
    echo "  [✗] 仍然无法连接，查看日志: docker logs careerloop-backend --tail 50"
