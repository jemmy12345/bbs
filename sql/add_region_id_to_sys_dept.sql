-- ----------------------------
-- 为 sys_dept 表添加 region_id 字段（区域ID：本部或分公司）
-- ----------------------------
-- 注意：如果字段已存在，执行此脚本会报错，可以忽略错误或先检查字段是否存在

-- 方法1：直接添加（如果字段已存在会报错，可以忽略）
ALTER TABLE sys_dept 
ADD COLUMN region_id BIGINT(20) DEFAULT NULL COMMENT '区域ID（本部或分公司）' AFTER del_flag;

-- 方法2：如果使用 MySQL 8.0+，可以使用以下方式（先检查是否存在）
-- 如果字段不存在则添加
-- SET @sql = IF(
--   (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
--    WHERE TABLE_SCHEMA = DATABASE() 
--    AND TABLE_NAME = 'sys_dept' 
--    AND COLUMN_NAME = 'region_id') = 0,
--   'ALTER TABLE sys_dept ADD COLUMN region_id BIGINT(20) DEFAULT NULL COMMENT ''区域ID（本部或分公司）'' AFTER del_flag',
--   'SELECT 1'
-- );
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;
