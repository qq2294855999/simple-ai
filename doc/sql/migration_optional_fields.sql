-- =====================================================
-- 技能返回格式 & 规则触发动作改为非必填
-- 日期: 2026-07-26
-- 说明: 将 agent_skill.return_data_format 和 agent_rule.trigger_action
--       从 NOT NULL 改为允许 NULL，使字段变为可选。
-- =====================================================

-- 技能表：返回格式改为可选
ALTER TABLE "public"."agent_skill"
    ALTER COLUMN "return_data_format" DROP NOT NULL;

-- 规则表：触发动作改为可选
ALTER TABLE "public"."agent_rule"
    ALTER COLUMN "trigger_action" DROP NOT NULL;