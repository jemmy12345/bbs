-- 提案评审流程演示账号与角色（可重复执行）
-- 演示账号密码均为：123456

INSERT INTO sys_role
(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '提案初审员', 'idea_l1_reviewer', 11, '1', 1, 1, '0', '0', 'admin', NOW(), '创意提案初审角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'idea_l1_reviewer' AND del_flag = '0');

INSERT INTO sys_role
(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '提案复审员', 'idea_l2_reviewer', 12, '1', 1, 1, '0', '0', 'admin', NOW(), '创意提案复审角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'idea_l2_reviewer' AND del_flag = '0');

INSERT INTO sys_role
(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '提案终审员', 'idea_l3_reviewer', 13, '1', 1, 1, '0', '0', 'admin', NOW(), '创意提案终审角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'idea_l3_reviewer' AND del_flag = '0');

INSERT INTO sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT 'l1reviewer', 103, 'l1reviewer', '初审员', '00', 'l1reviewer@example.com', '13800000101', '0', '',
       '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '提案初审演示账号'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_id = 'l1reviewer' OR user_name = 'l1reviewer');

INSERT INTO sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT 'l2reviewer', 103, 'l2reviewer', '复审员', '00', 'l2reviewer@example.com', '13800000102', '0', '',
       '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '提案复审演示账号'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_id = 'l2reviewer' OR user_name = 'l2reviewer');

INSERT INTO sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT 'l3reviewer', 103, 'l3reviewer', '终审员', '00', 'l3reviewer@example.com', '13800000103', '0', '',
       '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '提案终审演示账号'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_id = 'l3reviewer' OR user_name = 'l3reviewer');

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 'l1reviewer', role_id FROM sys_role WHERE role_key = 'idea_l1_reviewer' AND del_flag = '0' LIMIT 1;

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 'l2reviewer', role_id FROM sys_role WHERE role_key = 'idea_l2_reviewer' AND del_flag = '0' LIMIT 1;

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 'l3reviewer', role_id FROM sys_role WHERE role_key = 'idea_l3_reviewer' AND del_flag = '0' LIMIT 1;

UPDATE bbs_idea_stage_reviewer_config
SET role_keys = 'idea_l1_reviewer',
    reviewer_user_ids = 'l1reviewer',
    update_by = 'admin',
    update_time = NOW()
WHERE stage_code = 'L1';

UPDATE bbs_idea_stage_reviewer_config
SET role_keys = 'idea_l2_reviewer',
    reviewer_user_ids = 'l2reviewer',
    update_by = 'admin',
    update_time = NOW()
WHERE stage_code = 'L2';

UPDATE bbs_idea_stage_reviewer_config
SET role_keys = 'idea_l3_reviewer',
    reviewer_user_ids = 'l3reviewer',
    update_by = 'admin',
    update_time = NOW()
WHERE stage_code = 'L3';
