-- ============================================================
-- migration_v3_memory_lineage.sql
-- 记忆血脉追溯与校验逻辑修复
-- 变更内容：
--   1. agent_memory 表新增 parent_memory_id 字段（记忆血脉追溯）
--   2. 新增索引 idx_agent_memory_parent_memory_id（按父记忆查询版本链）
-- ============================================================

-- ========================
-- 一、agent_memory 表新增字段
-- ========================

-- 新增字段：父记忆ID，用于追溯记忆版本血缘关系
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "parent_memory_id" varchar (255) COLLATE "pg_catalog"."default" DEFAULT '';

-- 字段注释
COMMENT
ON COLUMN "public"."agent_memory"."parent_memory_id" IS '父记忆ID，修订场景下指向被修订的旧版本记忆，首次探索沉淀时为空';

-- 新增索引：按父记忆ID查询版本链
CREATE INDEX IF NOT EXISTS "idx_agent_memory_parent_memory_id" ON "public"."agent_memory" USING btree (
    "parent_memory_id" COLLATE "pg_catalog"."default" ASC NULLS LAST
    );