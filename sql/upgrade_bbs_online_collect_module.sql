-- Online collect module upgrade script
-- Compatible with existing bbs_vote / bbs_demand_form / bbs_topic_activity tables.

CREATE TABLE IF NOT EXISTS `bbs_collect_activity` (
  `activity_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `activity_type` VARCHAR(20) NOT NULL COMMENT 'vote/survey/demand/topic',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ENDED/DELETED',
  `creator_user_id` VARCHAR(64) NOT NULL,
  `creator_nick_name` VARCHAR(100) DEFAULT NULL,
  `scope_type` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT 'all/dept/users',
  `scope_value` VARCHAR(2000) DEFAULT NULL COMMENT 'dept ids or user ids split by comma',
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `anonymous_flag` CHAR(1) NOT NULL DEFAULT '0',
  `realtime_flag` CHAR(1) NOT NULL DEFAULT '1',
  `limit_once_flag` CHAR(1) NOT NULL DEFAULT '1',
  `biz_id` BIGINT DEFAULT NULL COMMENT 'row id from vote/form/topic table',
  `participant_count` INT NOT NULL DEFAULT 0,
  `banner_url` VARCHAR(500) DEFAULT NULL,
  `rule_text` VARCHAR(2000) DEFAULT NULL,
  `remark` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`),
  KEY `idx_collect_activity_type` (`activity_type`),
  KEY `idx_collect_activity_status` (`status`),
  KEY `idx_collect_activity_creator` (`creator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Online collect activity main table';

ALTER TABLE `bbs_vote`
  ADD COLUMN IF NOT EXISTS `is_anonymous` CHAR(1) NOT NULL DEFAULT '0',
  ADD COLUMN IF NOT EXISTS `show_realtime_result` CHAR(1) NOT NULL DEFAULT '1',
  ADD COLUMN IF NOT EXISTS `allow_multi_submit` CHAR(1) NOT NULL DEFAULT '0',
  ADD COLUMN IF NOT EXISTS `vote_scope_type` VARCHAR(20) NOT NULL DEFAULT 'all',
  ADD COLUMN IF NOT EXISTS `vote_scope_value` VARCHAR(2000) DEFAULT NULL;

ALTER TABLE `bbs_demand_form`
  ADD COLUMN IF NOT EXISTS `scope_type` VARCHAR(20) NOT NULL DEFAULT 'all',
  ADD COLUMN IF NOT EXISTS `scope_value` VARCHAR(2000) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `start_time` DATETIME DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `end_time` DATETIME DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `is_anonymous` CHAR(1) NOT NULL DEFAULT '0';

ALTER TABLE `bbs_topic_activity`
  ADD COLUMN IF NOT EXISTS `cover_url` VARCHAR(500) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `rule_text` VARCHAR(2000) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `scope_type` VARCHAR(20) NOT NULL DEFAULT 'all',
  ADD COLUMN IF NOT EXISTS `scope_value` VARCHAR(2000) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `archived_flag` CHAR(1) NOT NULL DEFAULT '0',
  ADD COLUMN IF NOT EXISTS `summary_text` VARCHAR(4000) DEFAULT NULL;

CREATE TABLE IF NOT EXISTS `bbs_collect_participation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `activity_type` VARCHAR(20) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `user_nick_name` VARCHAR(100) DEFAULT NULL,
  `submit_status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'DRAFT/SUBMITTED',
  `submit_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collect_participation_activity_user` (`activity_id`, `user_id`),
  KEY `idx_collect_participation_type` (`activity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Generic participation state';

CREATE TABLE IF NOT EXISTS `bbs_collect_survey_question` (
  `question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `question_no` INT NOT NULL,
  `question_type` VARCHAR(20) NOT NULL COMMENT 'single/multiple/fill/scale/text_line/text_area',
  `question_title` VARCHAR(1000) NOT NULL,
  `option_json` TEXT DEFAULT NULL,
  `jump_logic_json` TEXT DEFAULT NULL,
  `required_flag` CHAR(1) NOT NULL DEFAULT '1',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_collect_survey_q_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Survey questions';

CREATE TABLE IF NOT EXISTS `bbs_collect_survey_response` (
  `response_id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `user_nick_name` VARCHAR(100) DEFAULT NULL,
  `answers_json` LONGTEXT NOT NULL,
  `submit_status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED',
  `submit_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`response_id`),
  UNIQUE KEY `uk_collect_survey_activity_user` (`activity_id`, `user_id`),
  KEY `idx_collect_survey_response_status` (`submit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Survey responses and drafts';

CREATE TABLE IF NOT EXISTS `bbs_collect_demand_ticket` (
  `ticket_id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `form_id` BIGINT NOT NULL,
  `submit_user_id` VARCHAR(64) NOT NULL,
  `submit_nick_name` VARCHAR(100) DEFAULT NULL,
  `demand_name` VARCHAR(300) NOT NULL,
  `demand_type` VARCHAR(100) NOT NULL,
  `demand_detail` TEXT NOT NULL,
  `priority` VARCHAR(20) NOT NULL DEFAULT 'P3' COMMENT 'P0/P1/P2/P3',
  `attachment_urls` VARCHAR(1000) DEFAULT NULL,
  `contact_person` VARCHAR(100) DEFAULT NULL,
  `custom_field_json` TEXT DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/PROCESSING/DONE/REJECTED',
  `assignee_user_id` VARCHAR(64) DEFAULT NULL,
  `assignee_nick_name` VARCHAR(100) DEFAULT NULL,
  `process_comment` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`),
  KEY `idx_collect_demand_activity` (`activity_id`),
  KEY `idx_collect_demand_status` (`status`),
  KEY `idx_collect_demand_assignee` (`assignee_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand collection tickets';

CREATE TABLE IF NOT EXISTS `bbs_collect_topic_content` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `content_type` VARCHAR(20) NOT NULL COMMENT 'qa/idea/post/checkin/comment',
  `content_id` BIGINT NOT NULL,
  `content_title` VARCHAR(300) DEFAULT NULL,
  `author_user_id` VARCHAR(64) DEFAULT NULL,
  `author_nick_name` VARCHAR(100) DEFAULT NULL,
  `score` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_collect_topic_content_activity` (`activity_id`),
  KEY `idx_collect_topic_content_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Topic activity linked content';
