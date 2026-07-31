-- ============================================================
-- 记忆能力适配 - 删除版本机制 DDL
-- 数据库: PostgreSQL
-- @author qty
-- 说明: 记忆不再使用版本机制，修订改为直接覆盖当前记忆
--       删除 agent_memory 表版本相关字段 + task 表记忆版本快照字段
-- ============================================================

-- 删除 agent_memory 表版本相关字段
ALTER TABLE agent_memory DROP COLUMN IF EXISTS version_no;
ALTER TABLE agent_memory DROP COLUMN IF EXISTS version_status;
ALTER TABLE agent_memory DROP COLUMN IF EXISTS parent_memory_id;

-- 删除 task 表记忆版本快照字段
ALTER TABLE task DROP COLUMN IF EXISTS memory_version_no;
