-- 为提案评审配置表添加指定处理人字段
ALTER TABLE bbs_idea_stage_reviewer_config 
  ADD COLUMN reviewer_user_ids varchar(500) DEFAULT NULL COMMENT '指定处理人用户ID，逗号分隔，与role_keys并集判断';
