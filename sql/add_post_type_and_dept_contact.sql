-- ----------------------------
-- 添加帖子类型和回应部门字段
-- ----------------------------
ALTER TABLE bbs_post 
ADD COLUMN post_type VARCHAR(20) DEFAULT 'share' COMMENT '帖子类型（share分享、suggestion建议、opinion意见）' AFTER is_anonymous,
ADD COLUMN response_dept_id BIGINT(20) DEFAULT NULL COMMENT '回应部门ID' AFTER post_type;

-- ----------------------------
-- 部门接口人表
-- ----------------------------
DROP TABLE IF EXISTS bbs_dept_contact;
CREATE TABLE bbs_dept_contact (
  contact_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '接口人ID',
  dept_id            BIGINT(20)      NOT NULL                   COMMENT '部门ID',
  dept_name          VARCHAR(100)    NOT NULL                   COMMENT '部门名称',
  contact_name       VARCHAR(50)     NOT NULL                   COMMENT '部门接口人名称',
  contact_user_id    VARCHAR(64)     NOT NULL                   COMMENT '部门接口人ID（企业微信用户ID）',
  status             CHAR(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
  create_by          VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time        DATETIME                                   COMMENT '创建时间',
  update_by          VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time        DATETIME                                   COMMENT '更新时间',
  remark             VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (contact_id),
  UNIQUE KEY uk_dept_id (dept_id),
  KEY idx_dept_id (dept_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '部门接口人表';
