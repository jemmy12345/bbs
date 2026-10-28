CREATE TABLE IF NOT EXISTS `bbs_idea_stage_reviewer_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT,
  `stage_code` VARCHAR(20) NOT NULL COMMENT 'L1/L2/L3',
  `stage_name` VARCHAR(50) NOT NULL COMMENT '初审/复审/终审',
  `role_keys` VARCHAR(1000) NOT NULL COMMENT '角色标识，逗号分隔',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '0正常 1停用',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME DEFAULT NULL,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_idea_stage_code` (`stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创意提案流程评审角色配置';

INSERT INTO `bbs_idea_stage_reviewer_config` (`stage_code`, `stage_name`, `role_keys`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'L1', '初审', 'idea_l1_reviewer', '0', 'admin', NOW(), '默认初审角色'
WHERE NOT EXISTS (SELECT 1 FROM `bbs_idea_stage_reviewer_config` WHERE `stage_code` = 'L1');

INSERT INTO `bbs_idea_stage_reviewer_config` (`stage_code`, `stage_name`, `role_keys`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'L2', '复审', 'idea_l2_reviewer', '0', 'admin', NOW(), '默认复审角色'
WHERE NOT EXISTS (SELECT 1 FROM `bbs_idea_stage_reviewer_config` WHERE `stage_code` = 'L2');

INSERT INTO `bbs_idea_stage_reviewer_config` (`stage_code`, `stage_name`, `role_keys`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'L3', '终审', 'idea_l3_reviewer', '0', 'admin', NOW(), '默认终审角色'
WHERE NOT EXISTS (SELECT 1 FROM `bbs_idea_stage_reviewer_config` WHERE `stage_code` = 'L3');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置', 0, 98, '/admin/idea-review-config', 'Layout', '', 'BbsIdeaReviewerConfigRoot', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', NOW(), '论坛管理-提案评审配置目录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/admin/idea-review-config');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置', id_root.menu_id, 1, 'index', 'bbs/ideaStageConfig/index', '', 'BbsIdeaReviewerConfig', 1, 0, 'C', '0', '0', 'bbs:ideaStageConfig:list', 'edit', 'admin', NOW(), '论坛管理-提案评审配置菜单'
FROM (SELECT menu_id FROM sys_menu WHERE path = '/admin/idea-review-config' LIMIT 1) id_root
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'bbs:ideaStageConfig:list');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置查询', id_btn.menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'bbs:ideaStageConfig:query', '#', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'bbs:ideaStageConfig:list' LIMIT 1) id_btn
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'bbs:ideaStageConfig:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置新增', id_btn.menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'bbs:ideaStageConfig:add', '#', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'bbs:ideaStageConfig:list' LIMIT 1) id_btn
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'bbs:ideaStageConfig:add');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置修改', id_btn.menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'bbs:ideaStageConfig:edit', '#', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'bbs:ideaStageConfig:list' LIMIT 1) id_btn
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'bbs:ideaStageConfig:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '提案评审配置删除', id_btn.menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'bbs:ideaStageConfig:remove', '#', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'bbs:ideaStageConfig:list' LIMIT 1) id_btn
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'bbs:ideaStageConfig:remove');