-- ==========================================
-- 新增功能：每日签到 + 标签订阅
-- ==========================================

-- 1. 每日签到记录表
CREATE TABLE IF NOT EXISTS `bbs_checkin` (
  `checkin_id`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `user_id`       VARCHAR(64) NOT NULL COMMENT '用户ID',
  `checkin_date`  DATE        NOT NULL COMMENT '签到日期',
  `streak`        INT         NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `points_earned` INT         NOT NULL DEFAULT 2 COMMENT '本次获得积分',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`checkin_id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
  KEY `idx_checkin_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日签到记录';

-- 2. 标签订阅表
CREATE TABLE IF NOT EXISTS `bbs_tag_follow` (
  `follow_id`   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `user_id`     VARCHAR(64) NOT NULL COMMENT '用户ID',
  `tag_id`      BIGINT      NOT NULL COMMENT '标签ID',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
  PRIMARY KEY (`follow_id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_tag_follow_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签订阅';
