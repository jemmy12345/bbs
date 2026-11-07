-- 提案公示墙点赞/分享：确保点赞数、分享数字段存在，并按互动记录表回填（兼容 MySQL 8，可重复执行）

SET @sql = (SELECT IF(COUNT(*) = 0,
  "ALTER TABLE bbs_idea_proposal ADD COLUMN like_count INT DEFAULT 0 COMMENT '公示墙点赞数'",
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bbs_idea_proposal' AND COLUMN_NAME = 'like_count');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(COUNT(*) = 0,
  "ALTER TABLE bbs_idea_proposal ADD COLUMN share_count INT DEFAULT 0 COMMENT '公示墙分享数'",
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bbs_idea_proposal' AND COLUMN_NAME = 'share_count');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

UPDATE bbs_idea_proposal p
SET p.like_count = (SELECT COUNT(1) FROM bbs_idea_user_action a
                    WHERE a.idea_id = p.idea_id AND a.action_type = 'LIKE'),
    p.share_count = (SELECT COUNT(1) FROM bbs_idea_user_action a
                     WHERE a.idea_id = p.idea_id AND a.action_type = 'SHARE');
