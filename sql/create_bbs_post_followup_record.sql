-- 帖子闭环处理记录表
CREATE TABLE IF NOT EXISTS `bbs_post_followup_record` (
  `record_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `post_id`         bigint(20)   NOT NULL                COMMENT '帖子ID',
  `followup_status` varchar(20)  NOT NULL                COMMENT '处理状态（0待处理 1处理中 2已完成 3已关闭）',
  `process_note`    varchar(1000) DEFAULT NULL           COMMENT '处理备注',
  `owner_user_id`   varchar(64)  DEFAULT NULL            COMMENT '责任人用户ID',
  `owner_user_name` varchar(100) DEFAULT NULL            COMMENT '责任人姓名',
  `handled_by`      varchar(64)  DEFAULT NULL            COMMENT '处理人用户ID',
  `handled_by_name` varchar(100) DEFAULT NULL            COMMENT '处理人姓名',
  `handle_time`     datetime     DEFAULT NULL            COMMENT '处理时间',
  `create_by`       varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`        char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`record_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='帖子闭环处理记录';
