CREATE TABLE IF NOT EXISTS `bbs_idea_user_action` (
  `action_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `idea_id` bigint NOT NULL COMMENT '提案ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `action_type` varchar(20) NOT NULL COMMENT '动作类型：LIKE/SHARE',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`action_id`),
  UNIQUE KEY `uk_idea_user_action` (`idea_id`,`user_id`,`action_type`),
  KEY `idx_idea_user_action_user` (`user_id`),
  KEY `idx_idea_user_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提案用户互动记录';
