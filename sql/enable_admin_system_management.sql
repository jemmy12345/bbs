-- Enable admin backend menus and authorize admin role to maintain them.
-- Safe to execute multiple times.

-- 1) Ensure admin role is active
UPDATE sys_role
SET status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE role_key = 'admin';

-- 2) Make management catalogs visible and enabled (system / monitor / tool)
UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (1, 2, 3)
   OR parent_id IN (1, 2, 3);

-- 3) Grant all backend menu and button permissions to admin role
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON 1 = 1
WHERE r.role_key = 'admin';

-- 4) Ensure core system maintenance entries are visible and enabled
UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE perms IN (
    'system:user:list',
    'system:dept:list',
    'system:menu:list',
    'system:role:list',
    'system:post:list',
    'system:dict:list',
    'system:config:list',
    'system:notice:list'
);
