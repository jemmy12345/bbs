-- 基于 sys_user 的测试用户数据（可重复执行）
-- 说明：演示账号明文密码统一为 123456

-- 1) 用户信息
INSERT INTO sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT '2', 103, 'zhangsan', '张三', '00', 'zhangsan@example.com', '13800000001', '0', '', '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-张三'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'zhangsan');

INSERT INTO sys_user
(user_id,dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT '3', 103, 'lisi', '李四', '00', 'lisi@example.com', '13800000002', '1', '', '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-李四'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'lisi');

INSERT INTO sys_user
(user_id,dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT '4', 105, 'wangwu', '王五', '00', 'wangwu@example.com', '13800000003', '0', '', '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-王五'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'wangwu');

INSERT INTO sys_user
(user_id,dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
SELECT '5', 105, 'zhaoliu', '赵六', '00', 'zhaoliu@example.com', '13800000004', '2', '', '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-赵六'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'zhaoliu');

-- 2) 用户角色关联：统一赋予普通角色 role_id=2
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2
FROM sys_user u
WHERE u.user_name = 'zhangsan'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 2
  );

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2
FROM sys_user u
WHERE u.user_name = 'lisi'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 2
  );

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2
FROM sys_user u
WHERE u.user_name = 'wangwu'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 2
  );

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2
FROM sys_user u
WHERE u.user_name = 'zhaoliu'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = 2
  );

-- 3) 用户岗位关联：统一赋予普通员工 post_id=4
INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, 4
FROM sys_user u
WHERE u.user_name = 'zhangsan'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_post up WHERE up.user_id = u.user_id AND up.post_id = 4
  );

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, 4
FROM sys_user u
WHERE u.user_name = 'lisi'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_post up WHERE up.user_id = u.user_id AND up.post_id = 4
  );

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, 4
FROM sys_user u
WHERE u.user_name = 'wangwu'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_post up WHERE up.user_id = u.user_id AND up.post_id = 4
  );

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.user_id, 4
FROM sys_user u
WHERE u.user_name = 'zhaoliu'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_post up WHERE up.user_id = u.user_id AND up.post_id = 4
  );

-- 4) 校验
SELECT user_id, user_name, nick_name, phonenumber, status
FROM sys_user
WHERE user_name IN ('zhangsan', 'lisi', 'wangwu', 'zhaoliu')
ORDER BY user_id;
