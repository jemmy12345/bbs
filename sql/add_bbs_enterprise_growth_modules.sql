-- =====================================================
-- Enterprise Growth Modules
-- 1) QA Zone (bounty question, best answer, archive)
-- 2) Innovation Proposal Board
-- 3) Vote/Survey + Demand Form + Topic Activity
-- =====================================================

-- ----------------------------
-- 1. QA Zone
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bbs_qa_question` (
  `question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `reward_points` INT NOT NULL DEFAULT 10,
  `status` VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/solved/archived',
  `best_answer_id` BIGINT DEFAULT NULL,
  `ask_user_id` VARCHAR(64) NOT NULL,
  `ask_nick_name` VARCHAR(100) DEFAULT NULL,
  `close_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_qa_question_user` (`ask_user_id`),
  KEY `idx_qa_question_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Enterprise QA questions';

CREATE TABLE IF NOT EXISTS `bbs_qa_answer` (
  `answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `is_best` CHAR(1) NOT NULL DEFAULT '0',
  `answer_user_id` VARCHAR(64) NOT NULL,
  `answer_nick_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`answer_id`),
  KEY `idx_qa_answer_question` (`question_id`),
  KEY `idx_qa_answer_user` (`answer_user_id`),
  CONSTRAINT `fk_qa_answer_question` FOREIGN KEY (`question_id`) REFERENCES `bbs_qa_question` (`question_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Enterprise QA answers';

-- ----------------------------
-- 2. Innovation Proposal
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bbs_idea_proposal` (
  `idea_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `biz_value` VARCHAR(500) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'reviewing' COMMENT 'reviewing/executing/done/publicized/rejected',
  `avg_score` DECIMAL(5,2) NOT NULL DEFAULT 0,
  `result_summary` TEXT,
  `submit_user_id` VARCHAR(64) NOT NULL,
  `submit_nick_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idea_id`),
  KEY `idx_idea_status` (`status`),
  KEY `idx_idea_user` (`submit_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Innovation proposals';

CREATE TABLE IF NOT EXISTS `bbs_idea_review` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `idea_id` BIGINT NOT NULL,
  `reviewer_user_id` VARCHAR(64) NOT NULL,
  `reviewer_nick_name` VARCHAR(100) DEFAULT NULL,
  `score` INT NOT NULL,
  `comment` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `idx_idea_review_idea` (`idea_id`),
  KEY `idx_idea_review_user` (`reviewer_user_id`),
  CONSTRAINT `fk_idea_review_idea` FOREIGN KEY (`idea_id`) REFERENCES `bbs_idea_proposal` (`idea_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Innovation reviews';

CREATE TABLE IF NOT EXISTS `bbs_idea_progress` (
  `progress_id` BIGINT NOT NULL AUTO_INCREMENT,
  `idea_id` BIGINT NOT NULL,
  `progress_stage` VARCHAR(100) NOT NULL,
  `progress_desc` VARCHAR(1000) DEFAULT NULL,
  `progress_percent` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`progress_id`),
  KEY `idx_idea_progress_idea` (`idea_id`),
  CONSTRAINT `fk_idea_progress_idea` FOREIGN KEY (`idea_id`) REFERENCES `bbs_idea_proposal` (`idea_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Innovation execution progress';

-- ----------------------------
-- 3. Vote / Survey
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bbs_vote` (
  `vote_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `description` VARCHAR(1000) DEFAULT NULL,
  `vote_type` VARCHAR(20) NOT NULL DEFAULT 'single' COMMENT 'single/multiple',
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/closed',
  `creator_user_id` VARCHAR(64) NOT NULL,
  `creator_nick_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vote_id`),
  KEY `idx_vote_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Votes and surveys';

CREATE TABLE IF NOT EXISTS `bbs_vote_option` (
  `option_id` BIGINT NOT NULL AUTO_INCREMENT,
  `vote_id` BIGINT NOT NULL,
  `option_text` VARCHAR(300) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `vote_count` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`option_id`),
  KEY `idx_vote_option_vote` (`vote_id`),
  CONSTRAINT `fk_vote_option_vote` FOREIGN KEY (`vote_id`) REFERENCES `bbs_vote` (`vote_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vote options';

CREATE TABLE IF NOT EXISTS `bbs_vote_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT,
  `vote_id` BIGINT NOT NULL,
  `option_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_vote_option_user` (`vote_id`, `option_id`, `user_id`),
  KEY `idx_vote_record_user` (`user_id`),
  CONSTRAINT `fk_vote_record_vote` FOREIGN KEY (`vote_id`) REFERENCES `bbs_vote` (`vote_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vote_record_option` FOREIGN KEY (`option_id`) REFERENCES `bbs_vote_option` (`option_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vote records';

-- ----------------------------
-- 4. Demand Form
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bbs_demand_form` (
  `form_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `description` VARCHAR(1000) DEFAULT NULL,
  `form_schema` TEXT COMMENT 'json schema',
  `status` VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/closed',
  `creator_user_id` VARCHAR(64) NOT NULL,
  `creator_nick_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`form_id`),
  KEY `idx_demand_form_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand collection forms';

CREATE TABLE IF NOT EXISTS `bbs_demand_submission` (
  `submission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `form_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `user_nick_name` VARCHAR(100) DEFAULT NULL,
  `submission_content` TEXT NOT NULL COMMENT 'json content',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`submission_id`),
  KEY `idx_demand_submission_form` (`form_id`),
  KEY `idx_demand_submission_user` (`user_id`),
  CONSTRAINT `fk_demand_submission_form` FOREIGN KEY (`form_id`) REFERENCES `bbs_demand_form` (`form_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand submissions';

-- ----------------------------
-- 5. Topic Activity
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bbs_topic_activity` (
  `activity_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/closed',
  `join_count` INT NOT NULL DEFAULT 0,
  `creator_user_id` VARCHAR(64) NOT NULL,
  `creator_nick_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`),
  KEY `idx_topic_activity_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Topic activities';

CREATE TABLE IF NOT EXISTS `bbs_topic_activity_join` (
  `join_id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `user_nick_name` VARCHAR(100) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`join_id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_topic_join_user` (`user_id`),
  CONSTRAINT `fk_topic_join_activity` FOREIGN KEY (`activity_id`) REFERENCES `bbs_topic_activity` (`activity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Topic activity joins';
