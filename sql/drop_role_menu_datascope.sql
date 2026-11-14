-- 角色不再配置菜单权限和数据权限：管理员/普通角色/评审员由代码按角色标识判断
-- 删除关联表：sys_role_menu、sys_role_dept
-- 删除字段：sys_role.data_scope、sys_role.menu_check_strictly、sys_role.dept_check_strictly
-- 注意：
--   1) 执行后不可恢复，请先备份。
--   2) 只能执行一次；字段已不存在时会报 1091 错误并中止。
--   3) 请先更新并重启后端，再执行本脚本，或在停服窗口内执行。
USE bbsdb;

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_role_dept;

ALTER TABLE sys_role
  DROP COLUMN data_scope,
  DROP COLUMN menu_check_strictly,
  DROP COLUMN dept_check_strictly;
