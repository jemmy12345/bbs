-- 删除论文表3-3、表3-14中不再使用的字段
-- bbs_post:         summary、is_essence
-- bbs_qa_question:  audit_status、audit_reason（同时删除索引 idx_audit_status）
-- 注意：执行后不可恢复，请先备份。
USE bbsdb;

ALTER TABLE bbs_post
  DROP COLUMN summary,
  DROP COLUMN is_essence;

ALTER TABLE bbs_qa_question
  DROP INDEX idx_audit_status,
  DROP COLUMN audit_status,
  DROP COLUMN audit_reason;
