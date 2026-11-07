-- 删除 2026-05-01 之前创建的全部帖子及其关联数据（物理删除，不可恢复）
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_del_post AS
  SELECT post_id FROM bbs_post WHERE create_time < '2026-05-01 00:00:00';
CREATE TEMPORARY TABLE tmp_del_comment AS
  SELECT comment_id FROM bbs_comment WHERE post_id IN (SELECT post_id FROM tmp_del_post);

-- target_type：1 帖子 / 2 评论
DELETE FROM bbs_like         WHERE (target_type='1' AND target_id IN (SELECT post_id FROM tmp_del_post))
                                OR (target_type='2' AND target_id IN (SELECT comment_id FROM tmp_del_comment));
DELETE FROM bbs_dislike      WHERE (target_type='1' AND target_id IN (SELECT post_id FROM tmp_del_post))
                                OR (target_type='2' AND target_id IN (SELECT comment_id FROM tmp_del_comment));
DELETE FROM bbs_notification WHERE (target_type='1' AND target_id IN (SELECT post_id FROM tmp_del_post))
                                OR (target_type='2' AND target_id IN (SELECT comment_id FROM tmp_del_comment));
DELETE FROM bbs_collect      WHERE post_id IN (SELECT post_id FROM tmp_del_post);
DELETE FROM bbs_post_tag     WHERE post_id IN (SELECT post_id FROM tmp_del_post);
DELETE FROM bbs_post_followup_record WHERE post_id IN (SELECT post_id FROM tmp_del_post);
DELETE FROM bbs_comment      WHERE post_id IN (SELECT post_id FROM tmp_del_post);
DELETE FROM bbs_post         WHERE post_id IN (SELECT post_id FROM tmp_del_post);

drop table if exists tmp_del_post;
drop table if exists tmp_del_comment;

COMMIT;
