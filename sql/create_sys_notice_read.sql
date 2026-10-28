-- 通知公告已读记录：用户端右下角弹窗点击“已读”后写入，避免重复弹出
CREATE TABLE IF NOT EXISTS `sys_notice_read` (
  `notice_id` int NOT NULL COMMENT '公告ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  PRIMARY KEY (`notice_id`, `user_id`),
  KEY `idx_notice_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告已读记录表';
