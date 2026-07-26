-- ============================================================
-- 客户端在线状态管理 - 数据库变更脚本
-- 日期: 2026-07-26
-- 作者: qty
-- 说明: agent_client 表删除 machine_name 字段，新增 is_online 字段
-- 数据库: PostgreSQL
-- ============================================================

-- 删除机器名称字段
ALTER TABLE "public"."agent_client" DROP COLUMN IF EXISTS "machine_name";

-- 新增在线状态字段（PostgreSQL 原生 boolean 类型，与 Java Boolean 天然映射）
ALTER TABLE "public"."agent_client"
    ADD COLUMN IF NOT EXISTS "is_online" boolean NOT NULL DEFAULT false;

-- 更新字段注释
COMMENT
ON COLUMN "public"."agent_client"."is_online" IS '在线状态: false-离线 / true-在线，由WebSocket上下线机制同步';

-- 更新表注释（删除机器名称相关描述）
COMMENT
ON TABLE "public"."agent_client" IS '客户端实例，通过WebSocket上下线机制同步在线状态';