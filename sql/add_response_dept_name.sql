-- ----------------------------
-- 添加回应部门名称字段
-- ----------------------------
ALTER TABLE bbs_post 
ADD COLUMN response_dept_name VARCHAR(100) DEFAULT NULL COMMENT '回应部门名称' AFTER response_dept_id;
