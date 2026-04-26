-- ----------------------------
-- 添加评论表部门名称字段
-- ----------------------------
ALTER TABLE bbs_comment 
ADD COLUMN dept_name VARCHAR(100) DEFAULT NULL COMMENT '用户部门名称' AFTER avatar;
