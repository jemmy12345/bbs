-- =============================================
-- 将 bbs_post.last_reply_user 从 bigint 改为 varchar(64)
-- 其余 BBS 表 user_id 字段已在之前的迁移中完成更新：
--   bbs_collect.user_id          → varchar(64) ✅
--   bbs_comment.user_id          → varchar(64) ✅
--   bbs_comment.reply_user_id    → varchar(64) ✅
--   bbs_dislike.user_id          → varchar(64) ✅
--   bbs_follow.user_id           → varchar(64) ✅
--   bbs_follow.follow_user_id    → varchar(64) ✅
--   bbs_like.user_id             → varchar(64) ✅
--   bbs_notification.user_id     → varchar(64) ✅
--   bbs_notification.from_user_id→ varchar(64) ✅
--   bbs_post.user_id             → varchar(64) ✅
-- =============================================

ALTER TABLE bbs_post
    MODIFY COLUMN last_reply_user VARCHAR(64) DEFAULT NULL COMMENT '最后回复用户ID';
