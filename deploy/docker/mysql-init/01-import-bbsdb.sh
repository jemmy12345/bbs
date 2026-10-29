#!/bin/bash
# MySQL 首次初始化时导入 sql/bbsdb.sql
# 导出文件开头没有 SET NAMES，这里显式指定 utf8mb4，避免中文按 latin1 读入导致字段超长
set -e
echo "[smart-bbs] 导入 bbsdb.sql ..."
mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" --default-character-set=utf8mb4 "${MYSQL_DATABASE:-bbsdb}" < /sql/bbsdb.sql
echo "[smart-bbs] bbsdb.sql 导入完成"
