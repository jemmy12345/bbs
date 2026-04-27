-- 用户积分汇总字段（用于快速排行榜查询）
ALTER TABLE sys_user
    ADD COLUMN bbs_points INT NOT NULL DEFAULT 0 COMMENT '论坛积分' AFTER email;

-- 已有积分流水表补充复合索引，提升用户积分流水查询效率
ALTER TABLE bbs_point_record
    ADD INDEX idx_user_create_time (user_id, create_time);
