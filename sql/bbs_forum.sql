-- ----------------------------
-- 企业内部论坛系统数据库表
-- ----------------------------

-- ----------------------------
-- 1、论坛分类表
-- ----------------------------
drop table if exists bbs_category;
create table bbs_category (
  category_id       bigint(20)      not null auto_increment    comment '分类ID',
  category_name     varchar(50)     not null                   comment '分类名称',
  category_desc     varchar(500)    default ''                 comment '分类描述',
  icon              varchar(100)    default ''                 comment '分类图标',
  sort_order        int(4)          default 0                  comment '显示顺序',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  post_count        int(11)        default 0                  comment '帖子数量',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb auto_increment=1 comment = '论坛分类表';

-- ----------------------------
-- 初始化-论坛分类表数据
-- ----------------------------
insert into bbs_category values(1, '技术交流', '技术讨论和经验分享', 'el-icon-cpu', 1, '0', 0, 'admin', sysdate(), '', null, '');
insert into bbs_category values(2, '产品建议', '产品功能改进建议', 'el-icon-s-promotion', 2, '0', 0, 'admin', sysdate(), '', null, '');
insert into bbs_category values(3, '工作心得', '工作经验和心得体会', 'el-icon-document', 3, '0', 0, 'admin', sysdate(), '', null, '');
insert into bbs_category values(4, '生活分享', '生活趣事和日常分享', 'el-icon-sunny', 4, '0', 0, 'admin', sysdate(), '', null, '');
insert into bbs_category values(5, '问答求助', '技术问题和求助', 'el-icon-question', 5, '0', 0, 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 2、帖子表
-- ----------------------------
drop table if exists bbs_post;
create table bbs_post (
  post_id           bigint(20)      not null auto_increment    comment '帖子ID',
  category_id       bigint(20)      not null                   comment '分类ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  title             varchar(200)    not null                   comment '标题',
  content           text                                       comment '内容',
  summary           varchar(500)     default ''                 comment '摘要',
  view_count        int(11)         default 0                  comment '浏览数',
  like_count        int(11)         default 0                  comment '点赞数',
  comment_count     int(11)         default 0                  comment '评论数',
  collect_count     int(11)         default 0                  comment '收藏数',
  is_top            char(1)         default '0'                comment '是否置顶（0否 1是）',
  is_essence        char(1)         default '0'                comment '是否精华（0否 1是）',
  status            char(1)         default '0'                comment '状态（0正常 1关闭 2待审核）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  last_reply_time   datetime                                   comment '最后回复时间',
  last_reply_user   bigint(20)      default null               comment '最后回复用户ID',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (post_id),
  key idx_category_id (category_id),
  key idx_user_id (user_id),
  key idx_create_time (create_time),
  key idx_status (status)
) engine=innodb auto_increment=1 comment = '帖子表';

-- ----------------------------
-- 3、评论表
-- ----------------------------
drop table if exists bbs_comment;
create table bbs_comment (
  comment_id        bigint(20)      not null auto_increment    comment '评论ID',
  post_id           bigint(20)      not null                   comment '帖子ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  parent_id         bigint(20)      default 0                  comment '父评论ID（0表示一级评论）',
  reply_user_id     bigint(20)      default null               comment '回复的用户ID',
  content           text            not null                   comment '评论内容',
  like_count        int(11)         default 0                  comment '点赞数',
  status            char(1)         default '0'                comment '状态（0正常 1关闭）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (comment_id),
  key idx_post_id (post_id),
  key idx_user_id (user_id),
  key idx_parent_id (parent_id)
) engine=innodb auto_increment=1 comment = '评论表';

-- ----------------------------
-- 4、点赞表
-- ----------------------------
drop table if exists bbs_like;
create table bbs_like (
  like_id           bigint(20)      not null auto_increment    comment '点赞ID',
  target_type       char(1)         not null                   comment '目标类型（1帖子 2评论）',
  target_id         bigint(20)      not null                   comment '目标ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  create_time       datetime                                   comment '创建时间',
  primary key (like_id),
  unique key uk_target_user (target_type, target_id, user_id),
  key idx_user_id (user_id)
) engine=innodb auto_increment=1 comment = '点赞表';

-- ----------------------------
-- 5、收藏表
-- ----------------------------
drop table if exists bbs_collect;
create table bbs_collect (
  collect_id        bigint(20)      not null auto_increment    comment '收藏ID',
  post_id           bigint(20)      not null                   comment '帖子ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  create_time       datetime                                   comment '创建时间',
  primary key (collect_id),
  unique key uk_post_user (post_id, user_id),
  key idx_user_id (user_id)
) engine=innodb auto_increment=1 comment = '收藏表';

-- ----------------------------
-- 6、标签表
-- ----------------------------
drop table if exists bbs_tag;
create table bbs_tag (
  tag_id            bigint(20)      not null auto_increment    comment '标签ID',
  tag_name          varchar(50)     not null                   comment '标签名称',
  tag_color         varchar(20)     default '#409EFF'          comment '标签颜色',
  use_count         int(11)         default 0                  comment '使用次数',
  create_time       datetime                                   comment '创建时间',
  primary key (tag_id),
  unique key uk_tag_name (tag_name)
) engine=innodb auto_increment=1 comment = '标签表';

-- ----------------------------
-- 7、帖子标签关联表
-- ----------------------------
drop table if exists bbs_post_tag;
create table bbs_post_tag (
  id                bigint(20)      not null auto_increment    comment 'ID',
  post_id           bigint(20)      not null                   comment '帖子ID',
  tag_id            bigint(20)      not null                   comment '标签ID',
  primary key (id),
  unique key uk_post_tag (post_id, tag_id),
  key idx_post_id (post_id),
  key idx_tag_id (tag_id)
) engine=innodb auto_increment=1 comment = '帖子标签关联表';

-- ----------------------------
-- 8、关注表
-- ----------------------------
drop table if exists bbs_follow;
create table bbs_follow (
  follow_id         bigint(20)      not null auto_increment    comment '关注ID',
  user_id           bigint(20)      not null                   comment '用户ID（关注者）',
  follow_user_id    bigint(20)      not null                   comment '被关注用户ID',
  create_time       datetime                                   comment '创建时间',
  primary key (follow_id),
  unique key uk_user_follow (user_id, follow_user_id),
  key idx_user_id (user_id),
  key idx_follow_user_id (follow_user_id)
) engine=innodb auto_increment=1 comment = '关注表';

-- ----------------------------
-- 9、通知表
-- ----------------------------
drop table if exists bbs_notification;
create table bbs_notification (
  notification_id   bigint(20)      not null auto_increment    comment '通知ID',
  user_id           bigint(20)      not null                   comment '用户ID（接收者）',
  type              char(1)         not null                   comment '通知类型（1评论 2回复 3点赞 4关注 5系统）',
  title             varchar(200)    default ''                 comment '通知标题',
  content           varchar(500)    default ''                 comment '通知内容',
  target_type       char(1)         default ''                 comment '目标类型（1帖子 2评论）',
  target_id         bigint(20)      default null               comment '目标ID',
  from_user_id      bigint(20)      default null               comment '来源用户ID',
  is_read           char(1)         default '0'                comment '是否已读（0未读 1已读）',
  create_time       datetime                                   comment '创建时间',
  primary key (notification_id),
  key idx_user_id (user_id),
  key idx_is_read (is_read),
  key idx_create_time (create_time)
) engine=innodb auto_increment=1 comment = '通知表';
