-- 建议/意见类帖子闭环处理记录（正式表结构）
create table if not exists bbs_post_followup_record (
  record_id         bigint(20)      not null auto_increment comment '记录ID',
  post_id           bigint(20)      not null                comment '帖子ID',
  followup_status   varchar(32)     not null                comment '闭环状态（accepted/processing/feedback/resolved）',
  process_note      varchar(500)    default ''              comment '处理说明',
  owner_user_id     varchar(64)     default ''              comment '责任人ID',
  owner_user_name   varchar(100)    default ''              comment '责任人名称',
  handled_by        varchar(64)     default ''              comment '处理人账号',
  handled_by_name   varchar(100)    default ''              comment '处理人名称',
  handle_time       datetime                                comment '处理时间',
  create_by         varchar(64)     default ''              comment '创建者',
  create_time       datetime                                comment '创建时间',
  update_by         varchar(64)     default ''              comment '更新者',
  update_time       datetime                                comment '更新时间',
  del_flag          char(1)         default '0'             comment '删除标志（0存在 2删除）',
  primary key (record_id),
  key idx_post_id (post_id),
  key idx_status (followup_status),
  key idx_handle_time (handle_time)
) engine=innodb auto_increment=1 comment='帖子闭环处理记录表';
