-- 隐藏管理后台侧边栏中的 BBS 前台功能菜单
-- visible: '0'=显示, '1'=隐藏
-- 操作说明：在数据库中执行此脚本，重新登录管理后台后生效

UPDATE sys_menu
SET visible = '1'
WHERE menu_name IN (
    '企业论坛',
    '论坛专题',
    '企业增长专区',
    '企业问答专区',
    '问答知识库',
    '创意提案',
    '成果公示墙',
    '个人中心',
    '通知中心'
);

-- 若以上名称对应多条记录（父菜单+子按钮），可进一步限定只对目录/菜单类型生效：
-- UPDATE sys_menu SET visible = '1'
-- WHERE menu_name IN ('企业论坛', ...)
--   AND menu_type IN ('M', 'C');

-- 执行后可验证：
-- SELECT menu_id, menu_name, visible FROM sys_menu
-- WHERE menu_name IN ('企业论坛','论坛专题','企业增长专区','企业问答专区',
--                     '问答知识库','创意提案','成果公示墙','个人中心','通知中心');
