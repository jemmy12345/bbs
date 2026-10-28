-- =============================================
-- 企业问答专区 完整建表 SQL
-- 如表已存在则先删除旧表（开发环境用）
-- 生产环境请改为 CREATE TABLE IF NOT EXISTS
-- =============================================

DROP TABLE IF EXISTS bbs_qa_answer;
DROP TABLE IF EXISTS bbs_qa_question;

-- 问题表
CREATE TABLE bbs_qa_question (
    question_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    title           VARCHAR(200)  NOT NULL                COMMENT '问题标题',
    content         LONGTEXT      NOT NULL                COMMENT '问题内容（富文本）',
    category        VARCHAR(50)   NOT NULL DEFAULT 'tech' COMMENT '分类：tech技术/product产品/hr人事/admin行政/other其他',
    reward_points   INT           NOT NULL DEFAULT 10     COMMENT '悬赏积分（提交时冻结）',
    frozen_points   INT           NOT NULL DEFAULT 0      COMMENT '已冻结积分',
    status          VARCHAR(20)   NOT NULL DEFAULT 'open' COMMENT '状态：open待解答/solved已解决/archived已归档/cancelled已撤销',
    best_answer_id  BIGINT                                COMMENT '最佳答案ID',
    ask_user_id     VARCHAR(64)   NOT NULL                COMMENT '提问者用户ID',
    ask_nick_name   VARCHAR(100)                          COMMENT '提问者昵称',
    ask_avatar      VARCHAR(500)                          COMMENT '提问者头像',
    ask_dept_name   VARCHAR(100)                          COMMENT '提问者部门',
    visibility_scope VARCHAR(20)  NOT NULL DEFAULT 'all'  COMMENT '可见范围：all全员/dept部门/specific指定人员',
    visibility_extra VARCHAR(500)                         COMMENT '可见范围额外数据（部门ID/用户ID列表）',
    expiry_time     DATETIME                              COMMENT '问题有效期',
    close_time      DATETIME                              COMMENT '关闭/解决时间',
    archive_time    DATETIME                              COMMENT '归档时间',
    view_count      INT           NOT NULL DEFAULT 0      COMMENT '浏览数',
    answer_count    INT           NOT NULL DEFAULT 0      COMMENT '回答数',
    audit_status    VARCHAR(20)   NOT NULL DEFAULT 'approved' COMMENT '审核状态：pending待审核/approved已通过/rejected已拒绝',
    audit_reason    VARCHAR(500)                          COMMENT '审核不通过原因',
    tags            VARCHAR(500)                          COMMENT '关键词标签（逗号分隔）',
    del_flag        CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
    create_by       VARCHAR(64)                           COMMENT '创建者',
    update_by       VARCHAR(64)                           COMMENT '更新者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (question_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_ask_user_id (ask_user_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业问答-问题表';

-- 回答表
CREATE TABLE bbs_qa_answer (
    answer_id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '回答ID',
    question_id         BIGINT        NOT NULL                COMMENT '关联问题ID',
    parent_answer_id    BIGINT                                COMMENT '父回答ID（追问/补充楼中楼）',
    content             LONGTEXT      NOT NULL                COMMENT '回答内容（富文本）',
    is_best             CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '是否最佳答案（0否 1是）',
    like_count          INT           NOT NULL DEFAULT 0      COMMENT '点赞数',
    answer_user_id      VARCHAR(64)   NOT NULL                COMMENT '回答者用户ID',
    answer_nick_name    VARCHAR(100)                          COMMENT '回答者昵称',
    answer_avatar       VARCHAR(500)                          COMMENT '回答者头像',
    answer_dept_name    VARCHAR(100)                          COMMENT '回答者部门',
    del_flag            CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
    create_by           VARCHAR(64)                           COMMENT '创建者',
    update_by           VARCHAR(64)                           COMMENT '更新者',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (answer_id),
    INDEX idx_question_id (question_id),
    INDEX idx_answer_user_id (answer_user_id),
    INDEX idx_is_best (is_best)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业问答-回答表';
