-- 修复 bbs_notification 表的 user_id 和 from_user_id 字段类型
-- 将 bigint 改为 varchar，以支持字符串格式的用户ID（如邮箱）

-- 注意：执行前请先备份数据！

-- 如果表中有数据，先备份
-- CREATE TABLE bbs_notification_backup AS SELECT * FROM bbs_notification;

-- 修改 user_id 字段类型
ALTER TABLE bbs_notification MODIFY COLUMN user_id VARCHAR(64) NOT NULL COMMENT '用户ID（接收者）';

-- 修改 from_user_id 字段类型
ALTER TABLE bbs_notification MODIFY COLUMN from_user_id VARCHAR(64) DEFAULT NULL COMMENT '来源用户ID';

-- 如果 from_user_id 中有数字类型的值，需要先转换为字符串（如果之前有数据的话）
-- UPDATE bbs_notification SET from_user_id = CAST(from_user_id AS CHAR) WHERE from_user_id IS NOT NULL AND from_user_id REGEXP '^[0-9]+$';
