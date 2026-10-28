-- 断开历史积分流水与匿名帖子/匿名评论之间的关联
-- 背景：早期版本给匿名内容发放积分时，流水的 biz_id 记录了帖子/评论编号，
--       通过流水可以反查出匿名内容的真实作者。新版本匿名内容改用随机业务编号。
-- 作用：把历史流水中对应匿名内容的记录改为 post_anonymous / comment_anonymous，
--       biz_id 换成随机值（与具体内容不再有对应关系），用户积分总数不受影响。
-- 注意：执行前请备份 bbs_point_record 表；本脚本只需执行一次。

-- 匿名帖子
UPDATE bbs_point_record r
JOIN bbs_post p ON r.biz_type = 'post' AND r.biz_id = p.post_id AND p.is_anonymous = '1'
SET r.biz_type = 'post_anonymous',
    r.biz_id = FLOOR(1000000000000 + RAND() * 9000000000000000);

-- 匿名评论
UPDATE bbs_point_record r
JOIN bbs_comment c ON r.biz_type = 'comment' AND r.biz_id = c.comment_id AND c.is_anonymous = '1'
SET r.biz_type = 'comment_anonymous',
    r.biz_id = FLOOR(1000000000000 + RAND() * 9000000000000000);
