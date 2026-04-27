-- 论坛积分流水表（发帖/评论等行为积分记录）
CREATE TABLE IF NOT EXISTS bbs_point_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    action_type VARCHAR(32) NOT NULL COMMENT '行为类型：POST_PUBLISH/COMMENT_PUBLISH',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型：post/comment',
    biz_id BIGINT NOT NULL COMMENT '业务ID',
    point_change INT NOT NULL DEFAULT 0 COMMENT '积分变更值，正数增加，负数扣减',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_point_action_biz (user_id, action_type, biz_type, biz_id),
    KEY idx_point_user (user_id),
    KEY idx_point_action (action_type),
    KEY idx_point_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛积分流水表';
