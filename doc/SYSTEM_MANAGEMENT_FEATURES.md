# 系统管理功能梳理（当前项目）

## 一、系统管理前端页面（cug-bbs-ui/src/views/system）

- 账号管理：`user/index.vue`
- 角色管理：`role/index.vue`
- 组织管理：`dept/index.vue`
- 菜单管理：`menu/index.vue`
- 岗位管理：`post/index.vue`
- 字典管理：`dict/index.vue`
- 参数配置：`config/index.vue`
- 通知公告：`notice/index.vue`
- 个人中心：`user/profile/index.vue`

## 二、系统管理后端控制器（ruoyi-admin/.../controller/system）

- `SysUserController`
- `SysRoleController`
- `SysDeptController`
- `SysMenuController`
- `SysPostController`
- `SysDictTypeController`
- `SysDictDataController`
- `SysConfigController`
- `SysNoticeController`

## 三、核心权限点（节选）

### 1) 用户
- `system:user:list`
- `system:user:query`
- `system:user:add`
- `system:user:edit`
- `system:user:remove`
- `system:user:export`
- `system:user:import`
- `system:user:resetPwd`

### 2) 角色
- `system:role:list`
- `system:role:query`
- `system:role:add`
- `system:role:edit`
- `system:role:remove`
- `system:role:export`

### 3) 部门
- `system:dept:list`
- `system:dept:query`
- `system:dept:add`
- `system:dept:edit`
- `system:dept:remove`

### 4) 菜单
- `system:menu:list`
- `system:menu:query`
- `system:menu:add`
- `system:menu:edit`
- `system:menu:remove`

### 5) 其他基础能力
- 岗位：`system:post:*`
- 字典：`system:dict:*`
- 参数：`system:config:*`
- 公告：`system:notice:*`

## 四、已完成开放策略

1. 已在管理员管理端（`/admin`）新增系统基础管理入口：
- 账号管理
- 角色管理
- 组织管理
- 菜单管理
- 岗位管理
- 字典管理
- 参数配置
- 通知公告

2. 已恢复前端动态权限路由加载逻辑（按后端菜单权限加载）。

3. 已提供 SQL 脚本：`sql/enable_admin_system_management.sql`
- 激活 admin 角色
- 开放系统/监控/工具目录可见可用
- 将全部菜单权限授权给 admin 角色
- 强化系统基础管理核心入口可见可用

## 五、落地建议

1. 先执行：`sql/enable_admin_system_management.sql`
2. 让管理员账号重新登录
3. 在管理员管理端 `/admin` 页面直接使用系统基础数据维护功能
