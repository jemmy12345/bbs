-- 汇总：删除论文表中不再使用的字段（第一轮 + 第二轮，共 10 个字段）
--   bbs_post:           summary、is_essence
--   bbs_qa_question:    audit_status、audit_reason、visibility_scope、visibility_extra（同时删除索引 idx_audit_status）
--   bbs_qa_answer:      parent_answer_id、like_count
--   bbs_idea_proposal:  project_time
--   sys_user:           user_type
-- 注意：
--   1) 执行后不可恢复，请先备份。
--   2) 只能执行一次；字段已不存在时会报 1091 错误并中止。
--   3) 对应的后端代码已不再引用这些字段，请先更新并重启后端镜像，或在停服窗口内执行。
USE bbsdb;

ALTER TABLE bbs_post
  DROP COLUMN summary,
  DROP COLUMN is_essence;

ALTER TABLE bbs_qa_question
  DROP INDEX idx_audit_status,
  DROP COLUMN audit_status,
  DROP COLUMN audit_reason,
  DROP COLUMN visibility_scope,
  DROP COLUMN visibility_extra;

ALTER TABLE bbs_qa_answer
  DROP COLUMN parent_answer_id,
  DROP COLUMN like_count;

ALTER TABLE bbs_idea_proposal
  DROP COLUMN project_time;

ALTER TABLE sys_user
  DROP COLUMN user_type;
