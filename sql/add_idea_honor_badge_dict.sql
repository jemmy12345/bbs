-- 创意提案成果公示：荣誉勋章字典（字典值与标签一致，与已有公示数据兼容）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '提案荣誉勋章', 'bbs_idea_honor_badge', '0', 'admin', NOW(), '创意提案成果公示可选荣誉勋章'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'bbs_idea_honor_badge');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.s, t.l, t.l, 'bbs_idea_honor_badge', '', 'default', t.d, '0', 'admin', NOW(), t.r
FROM (
  SELECT 1 s, '创新先锋' l, 'Y' d, '落地成效突出的创新提案' r UNION ALL
  SELECT 2, '降本增效之星', 'N', '显著降低成本或提升效率' UNION ALL
  SELECT 3, '流程优化能手', 'N', '优化业务流程成效明显' UNION ALL
  SELECT 4, '技术突破奖', 'N', '技术方案有突破性创新' UNION ALL
  SELECT 5, '管理创新奖', 'N', '管理模式或机制创新' UNION ALL
  SELECT 6, '团队协作奖', 'N', '跨部门协作落地的提案' UNION ALL
  SELECT 7, '最佳落地奖', 'N', '落地质量与推广价值突出'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'bbs_idea_honor_badge');
