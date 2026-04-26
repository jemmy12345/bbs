-- ============================================
-- 添加审核不通过原因字段 - 生产环境执行脚本
-- 执行前请确保：
-- 1. 已备份数据库
-- 2. 在业务低峰期执行
-- 3. 已通知相关人员
-- ============================================

-- 检查字段是否已存在（如果已存在则跳过）
SET @dbname = DATABASE();
SET @tablename = 'bbs_post';
SET @columnname = 'audit_reason';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''Column audit_reason already exists.'' AS result;',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(500) DEFAULT NULL COMMENT ''审核不通过原因'' AFTER status;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 更新status字段注释（如果字段已存在，这个操作是安全的）
ALTER TABLE bbs_post MODIFY COLUMN status CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1关闭 2待审核 3审核不通过）';

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'bbs_post'
  AND COLUMN_NAME = 'audit_reason';
