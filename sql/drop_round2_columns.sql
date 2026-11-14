-- 第二轮删除论文表中不再使用的字段（已核对代码无引用、无外键/索引依赖）
-- sys_user:           user_type
-- bbs_idea_proposal:  project_time
-- bbs_qa_question:    visibility_scope、visibility_extra
-- bbs_qa_answer:      parent_answer_id、like_count
-- 注意：执行后不可恢复，请先备份。
USE bbsdb;

ALTER TABLE sys_user
  DROP COLUMN user_type;

ALTER TABLE bbs_idea_proposal
  DROP COLUMN project_time;

ALTER TABLE bbs_qa_question
  DROP COLUMN visibility_scope,
  DROP COLUMN visibility_extra;

ALTER TABLE bbs_qa_answer
  DROP COLUMN parent_answer_id,
  DROP COLUMN like_count;
