-- 紧急修复：修改 bbs_notification 表的字段类型以支持字符串格式的用户ID
-- 请直接执行此SQL脚本

-- 修改 user_id 字段类型（从 bigint 改为 varchar）
ALTER TABLE bbs_notification MODIFY COLUMN user_id VARCHAR(64) NOT NULL COMMENT '用户ID（接收者）';

-- 修改 from_user_id 字段类型（从 bigint 改为 varchar）
ALTER TABLE bbs_notification MODIFY COLUMN from_user_id VARCHAR(64) DEFAULT NULL COMMENT '来源用户ID';

-- 执行完成后，评论接口应该可以正常工作了
