-- ============================================
-- 添加审核不通过原因字段 - 简单版本
-- 适用于生产环境，执行前请备份数据库
-- ============================================

-- 添加审核不通过原因字段
ALTER TABLE bbs_post ADD COLUMN audit_reason VARCHAR(500) DEFAULT NULL COMMENT '审核不通过原因' AFTER status;

-- 更新status字段注释，添加审核不通过状态
ALTER TABLE bbs_post MODIFY COLUMN status CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1关闭 2待审核 3审核不通过）';
