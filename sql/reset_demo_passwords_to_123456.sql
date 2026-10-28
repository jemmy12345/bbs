-- 将本项目演示账号统一重置为 123456，方便前台/后台流程验收。
-- BCrypt(123456)
UPDATE sys_user
SET password = '$2a$10$tMTrgqQAiSDsYSUzJyx8c.PwO6ED2K8gC/l8R/KaAgM64wfnT9suG',
    pwd_update_date = NOW(),
    update_by = 'admin',
    update_time = NOW()
WHERE user_name IN (
  'admin',
  'ry',
  'zhangsan',
  'lisi',
  'wangwu',
  'zhaoliu',
  'l1reviewer',
  'l2reviewer',
  'l3reviewer'
);
