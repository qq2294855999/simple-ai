-- =====================================================
-- 技能/规则/智能体定义 MD 化改造 - 回滚 SQL
-- 日期: 2026-07-26
-- 说明: 删除此前错误新增的 content 字段。
--       MD 编辑功能已改为直接使用现有 definition_desc、
--       exec_content 字段，无需额外 content 列。
-- =====================================================

-- 技能表删除 content 字段
ALTER TABLE "public"."agent_skill" DROP COLUMN IF EXISTS "content";

-- 规则表删除 content 字段
ALTER TABLE "public"."agent_rule" DROP COLUMN IF EXISTS "content";

-- 智能体定义表删除 content 字段
ALTER TABLE "public"."agent_definition" DROP COLUMN IF EXISTS "content";