-- ============================================================
-- 迁移脚本：记忆系统重构 + 级联删除统计补全
-- 日期：2026-07-25
-- 说明：
--   1. agent_memory 表结构重构（新增字段、删除废弃字段）
--   2. 新建 agent_memory_step 表（替代 agent_memory_detail）
--   3. task 表字段变更（agent_memory_id → memory_id，新增 memory_version_no）
--   4. 删除废弃表（agent_memory_detail、agent_memory_version、agent_memory_version_detail、memory_evidence）
--   5. 索引调整
-- ============================================================

-- ========================
-- 一、agent_memory 表重构
-- ========================

-- 新增字段
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "params_definition" text NOT NULL DEFAULT '{}';
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "version_no" int4 NOT NULL DEFAULT 1;
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "version_status" int2 NOT NULL DEFAULT 1;
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "source_task_id" varchar (255) COLLATE "pg_catalog"."default" DEFAULT '';
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "summary" text NOT NULL DEFAULT '';
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "create_reason" varchar (64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'MANUAL';
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "client_id" varchar (255) COLLATE "pg_catalog"."default" DEFAULT '';
ALTER TABLE "public"."agent_memory"
    ADD COLUMN IF NOT EXISTS "create_user_id" varchar (255) COLLATE "pg_catalog"."default" DEFAULT '';

-- 字段注释
COMMENT
ON COLUMN "public"."agent_memory"."params_definition" IS '参数定义JSON，描述每个占位符的类型和含义';
COMMENT
ON COLUMN "public"."agent_memory"."version_no" IS '当前版本号，同一记忆下递增，从1开始';
COMMENT
ON COLUMN "public"."agent_memory"."version_status" IS '版本状态: 1-DRAFT(草稿) / 2-PUBLISHED(已发布) / 3-RETIRED(已退役)';
COMMENT
ON COLUMN "public"."agent_memory"."source_task_id" IS '来源任务主键，记录该版本由哪个任务产生';
COMMENT
ON COLUMN "public"."agent_memory"."summary" IS '记忆摘要，步骤名称拼接的简要描述';
COMMENT
ON COLUMN "public"."agent_memory"."create_reason" IS '创建原因: MANUAL(手动) / AI_EXPLORATION(AI探索沉淀) / MEMORY_REVISE(失败修订)';
COMMENT
ON COLUMN "public"."agent_memory"."client_id" IS '执行客户端主键，关联 agent_client.id';
COMMENT
ON COLUMN "public"."agent_memory"."create_user_id" IS '创建人用户ID';

-- 更新已有注释
COMMENT
ON COLUMN "public"."agent_memory"."memory_name" IS '记忆名称模板，支持{param}占位符';

-- 删除废弃字段
ALTER TABLE "public"."agent_memory" DROP COLUMN IF EXISTS "step_name";
ALTER TABLE "public"."agent_memory" DROP COLUMN IF EXISTS "trigger_condition";
ALTER TABLE "public"."agent_memory" DROP COLUMN IF EXISTS "trigger_action";

-- 新增索引：按版本状态查询已发布记忆
CREATE INDEX IF NOT EXISTS "idx_agent_memory_version_status" ON "public"."agent_memory" USING btree (
    "agent_id" COLLATE "pg_catalog"."default" ASC NULLS LAST,
    "version_status" "pg_catalog"."int2_ops" ASC NULLS LAST
    );

-- 新增索引：按用户ID查询记忆
CREATE INDEX IF NOT EXISTS "idx_agent_memory_user_id" ON "public"."agent_memory" USING btree (
    "user_id" COLLATE "pg_catalog"."default" ASC NULLS LAST
    );


-- ========================
-- 二、新建 agent_memory_step 表
-- ========================

DROP TABLE IF EXISTS "public"."agent_memory_step";
CREATE TABLE "public"."agent_memory_step"
(
    "id"                  varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "memory_id"           varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "sequence_no"         int4                                        NOT NULL,
    "atomic_command_id"   varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "atomic_command_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "step_name"           varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "args_template"       text COLLATE "pg_catalog"."default",
    "delay_min_ms"        int4                                                 DEFAULT 100,
    "delay_max_ms"        int4                                                 DEFAULT 500,
    "timeout_ms"          int4                                                 DEFAULT 30000,
    "success_assertion"   text COLLATE "pg_catalog"."default",
    "failure_strategy"    varchar(32) COLLATE "pg_catalog"."default"           DEFAULT 'STOP'::character varying,
    "status"              varchar(16) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying,
    "create_time"         timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time"         timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT
ON COLUMN "public"."agent_memory_step"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_memory_step"."memory_id" IS '记忆主键，关联 agent_memory.id';
COMMENT
ON COLUMN "public"."agent_memory_step"."sequence_no" IS '步骤序号，同一记忆内从10开始递增，决定执行顺序';
COMMENT
ON COLUMN "public"."agent_memory_step"."atomic_command_id" IS '原子命令主键，关联 atomic_command.id';
COMMENT
ON COLUMN "public"."agent_memory_step"."atomic_command_code" IS '原子命令编码（冗余），如 weixin_search_contact';
COMMENT
ON COLUMN "public"."agent_memory_step"."step_name" IS '步骤名称，如"搜索联系人"';
COMMENT
ON COLUMN "public"."agent_memory_step"."args_template" IS '参数模板JSON，支持{param}占位符，运行时替换为实际参数';
COMMENT
ON COLUMN "public"."agent_memory_step"."delay_min_ms" IS '执行前随机延迟最小值（毫秒），默认100';
COMMENT
ON COLUMN "public"."agent_memory_step"."delay_max_ms" IS '执行前随机延迟最大值（毫秒），默认500';
COMMENT
ON COLUMN "public"."agent_memory_step"."timeout_ms" IS '命令超时时间（毫秒），默认30000';
COMMENT
ON COLUMN "public"."agent_memory_step"."success_assertion" IS '成功断言规则，用于判断该步骤是否执行成功';
COMMENT
ON COLUMN "public"."agent_memory_step"."failure_strategy" IS '失败处理策略: STOP(停止) / RETRY(重试) / SKIP(跳过)';
COMMENT
ON COLUMN "public"."agent_memory_step"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."agent_memory_step"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_memory_step"."update_time" IS '修改时间';
COMMENT
ON TABLE "public"."agent_memory_step" IS '智能体记忆步骤';

-- 主键
ALTER TABLE "public"."agent_memory_step"
    ADD CONSTRAINT "agent_memory_step_pkey" PRIMARY KEY ("id");

-- 索引：按记忆ID + 序号查询步骤
CREATE INDEX "idx_agent_memory_step_memory_seq" ON "public"."agent_memory_step" USING btree (
    "memory_id" COLLATE "pg_catalog"."default" ASC NULLS LAST,
    "sequence_no" "pg_catalog"."int4_ops" ASC NULLS LAST
    );


-- ========================
-- 三、task 表字段变更
-- ========================

-- 新增 memory_id 字段（替代 agent_memory_id）
ALTER TABLE "public"."task"
    ADD COLUMN IF NOT EXISTS "memory_id" varchar (255) COLLATE "pg_catalog"."default" DEFAULT '';

-- 新增 memory_version_no 字段（执行时的记忆版本号快照）
ALTER TABLE "public"."task"
    ADD COLUMN IF NOT EXISTS "memory_version_no" int4;

-- 字段注释
COMMENT
ON COLUMN "public"."task"."memory_id" IS '关联记忆主键，对应 agent_memory.id';
COMMENT
ON COLUMN "public"."task"."memory_version_no" IS '执行时的记忆版本号快照';

-- 数据迁移：将 agent_memory_id 的值复制到 memory_id
UPDATE "public"."task"
SET "memory_id" = "agent_memory_id"
WHERE "agent_memory_id" IS NOT NULL
  AND "agent_memory_id" != '';

-- 数据迁移：将 memory_version_id 的值解析为版本号写入 memory_version_no
-- （由于 memory_version_id 关联的是已删除的 agent_memory_version 表，此处置空）
UPDATE "public"."task"
SET "memory_version_no" = 1
WHERE "memory_id" IS NOT NULL
  AND "memory_id" != '';

-- 删除废弃字段 agent_memory_id
ALTER TABLE "public"."task" DROP COLUMN IF EXISTS "agent_memory_id";

-- 删除废弃字段 memory_version_id（关联已删除的 agent_memory_version 表）
ALTER TABLE "public"."task" DROP COLUMN IF EXISTS "memory_version_id";

-- 新增索引：按记忆ID查询任务
CREATE INDEX IF NOT EXISTS "idx_task_memory_id" ON "public"."task" USING btree (
    "memory_id" COLLATE "pg_catalog"."default" ASC NULLS LAST
    );

-- 删除旧索引（基于 agent_memory_id 的索引已无效）
DROP INDEX IF EXISTS "public"."idx_task_memory_status";


-- ========================
-- 四、删除废弃表
-- ========================

-- 删除 agent_memory_detail（被 agent_memory_step 替代）
DROP TABLE IF EXISTS "public"."agent_memory_detail" CASCADE;

-- 删除 agent_memory_version（版本信息已合并到 agent_memory）
DROP TABLE IF EXISTS "public"."agent_memory_version" CASCADE;

-- 删除 agent_memory_version_detail（被 agent_memory_step 替代）
DROP TABLE IF EXISTS "public"."agent_memory_version_detail" CASCADE;

-- 删除 memory_evidence（不再使用）
DROP TABLE IF EXISTS "public"."memory_evidence" CASCADE;


-- ========================
-- 五、验证查询
-- ========================

-- 验证 agent_memory 新字段
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'agent_memory'
ORDER BY ordinal_position;

-- 验证 agent_memory_step 表
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'agent_memory_step'
ORDER BY ordinal_position;

-- 验证 task 表字段
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'task'
  AND column_name IN ('memory_id', 'memory_version_no', 'agent_memory_id', 'memory_version_id')
ORDER BY ordinal_position;

-- 验证废弃表已删除
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('agent_memory_detail', 'agent_memory_version', 'agent_memory_version_detail', 'memory_evidence');