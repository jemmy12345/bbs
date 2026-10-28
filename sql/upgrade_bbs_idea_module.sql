-- 创意提案模块增强（增量）
-- 执行前请先备份数据库

ALTER TABLE bbs_idea_proposal
  ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'other' COMMENT '提案分类(product/tech/manage/cost/other)' AFTER title,
  ADD COLUMN IF NOT EXISTS background_text TEXT COMMENT '提案背景' AFTER content,
  ADD COLUMN IF NOT EXISTS plan_text LONGTEXT COMMENT '实施方案' AFTER background_text,
  ADD COLUMN IF NOT EXISTS expected_value TEXT COMMENT '预期价值' AFTER plan_text,
  ADD COLUMN IF NOT EXISTS visibility_scope VARCHAR(20) DEFAULT 'review_group' COMMENT '可见范围(all/review_group/admin)' AFTER expected_value,
  ADD COLUMN IF NOT EXISTS attachment_urls TEXT COMMENT '附件URL，逗号分隔' AFTER visibility_scope,
  ADD COLUMN IF NOT EXISTS related_case TEXT COMMENT '相关案例' AFTER attachment_urls,
  ADD COLUMN IF NOT EXISTS team_name VARCHAR(100) COMMENT '提案人团队' AFTER related_case,
  ADD COLUMN IF NOT EXISTS is_draft CHAR(1) DEFAULT '0' COMMENT '是否草稿(0否1是)' AFTER team_name,
  ADD COLUMN IF NOT EXISTS review_stage VARCHAR(20) DEFAULT 'L1' COMMENT '当前评审环节(L1/L2/L3/NONE)' AFTER is_draft,
  ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(1000) COMMENT '驳回原因' AFTER review_stage,
  ADD COLUMN IF NOT EXISTS project_time DATETIME COMMENT '立项时间' AFTER reject_reason,
  ADD COLUMN IF NOT EXISTS landing_time DATETIME COMMENT '落地时间' AFTER project_time,
  ADD COLUMN IF NOT EXISTS is_publicized CHAR(1) DEFAULT '0' COMMENT '是否公示(0否1是)' AFTER landing_time,
  ADD COLUMN IF NOT EXISTS incentive_points INT DEFAULT 0 COMMENT '创新激励积分' AFTER is_publicized,
  ADD COLUMN IF NOT EXISTS honor_badge VARCHAR(100) COMMENT '荣誉勋章' AFTER incentive_points,
  ADD COLUMN IF NOT EXISTS like_count INT DEFAULT 0 COMMENT '公示墙点赞数' AFTER honor_badge,
  ADD COLUMN IF NOT EXISTS share_count INT DEFAULT 0 COMMENT '公示墙分享数' AFTER like_count,
  ADD COLUMN IF NOT EXISTS review_deadline DATETIME COMMENT '当前环节评审截止时间' AFTER share_count,
  ADD COLUMN IF NOT EXISTS del_flag CHAR(1) DEFAULT '0' COMMENT '删除标识(0存在2删除)' AFTER review_deadline;

ALTER TABLE bbs_idea_review
  ADD COLUMN IF NOT EXISTS stage VARCHAR(20) DEFAULT 'L1' COMMENT '评审环节(L1/L2/L3)' AFTER idea_id,
  ADD COLUMN IF NOT EXISTS action VARCHAR(20) DEFAULT 'pass' COMMENT '评审动作(pass/reject/project)' AFTER stage,
  ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(1000) COMMENT '驳回原因' AFTER comment,
  ADD COLUMN IF NOT EXISTS is_visible_to_all CHAR(1) DEFAULT '0' COMMENT '意见是否对全员可见(0否1是)' AFTER reject_reason,
  ADD COLUMN IF NOT EXISTS review_time DATETIME COMMENT '审核时间' AFTER is_visible_to_all;

ALTER TABLE bbs_idea_progress
  ADD COLUMN IF NOT EXISTS status_enum VARCHAR(30) COMMENT '状态枚举' AFTER idea_id,
  ADD COLUMN IF NOT EXISTS updater_user_id VARCHAR(64) COMMENT '更新人ID' AFTER status_enum,
  ADD COLUMN IF NOT EXISTS updater_nick_name VARCHAR(100) COMMENT '更新人昵称' AFTER updater_user_id,
  ADD COLUMN IF NOT EXISTS attachment_urls TEXT COMMENT '落地资料/附件URL' AFTER progress_desc;

CREATE INDEX IF NOT EXISTS idx_idea_category ON bbs_idea_proposal(category);
CREATE INDEX IF NOT EXISTS idx_idea_publicized ON bbs_idea_proposal(is_publicized);
CREATE INDEX IF NOT EXISTS idx_idea_stage ON bbs_idea_proposal(review_stage);
CREATE INDEX IF NOT EXISTS idx_idea_deadline ON bbs_idea_proposal(review_deadline);
CREATE INDEX IF NOT EXISTS idx_idea_review_stage ON bbs_idea_review(stage);
