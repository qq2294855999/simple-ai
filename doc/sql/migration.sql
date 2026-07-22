-- ============================================================
-- Simple AI — 开发期非兼容迁移 SQL
-- 版本: v2.0
-- 作者: qty
-- 日期: 2026-07-22
-- 数据库: PostgreSQL 14
-- 说明: 开发阶段，不做兼容，直接执行 DDL。
--       包含：新增表 / ALTER字段 / 修复拼写 / 补齐注释 / 删除废弃
-- ============================================================

-- ============================================================
-- 第一部分：新增表
-- ============================================================

-- ----------------------------
-- 1. chat_turn（对话轮次）
-- 用途: 关联一条 USER 消息和一条 ASSISTANT 消息为一个轮次
-- ----------------------------
DROP TABLE IF EXISTS "public"."chat_turn";
CREATE TABLE "public"."chat_turn"
(
    "id"                   varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "session_id"           varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "turn_number"          int4                                        NOT NULL,
    "user_message_id"      varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "assistant_message_id" varchar(64) COLLATE "pg_catalog"."default",
    "task_id"              varchar(64) COLLATE "pg_catalog"."default",
    "reasoning_summary"    jsonb,
    "create_time"          timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time"          timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"               varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying,
    "reserve"              text COLLATE "pg_catalog"."default",
    "remark"               varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT
ON COLUMN "public"."chat_turn"."id" IS '轮次主键，UUID';
COMMENT
ON COLUMN "public"."chat_turn"."session_id" IS '会话主键，关联 agent_chat_session.id';
COMMENT
ON COLUMN "public"."chat_turn"."turn_number" IS '会话内轮次序号，从1递增';
COMMENT
ON COLUMN "public"."chat_turn"."user_message_id" IS '该轮用户消息ID，关联 agent_chat_message.id';
COMMENT
ON COLUMN "public"."chat_turn"."assistant_message_id" IS '该轮AI回复消息ID，关联 agent_chat_message.id（AI回复完成前为NULL）';
COMMENT
ON COLUMN "public"."chat_turn"."task_id" IS '关联的调度任务ID（冗余便于查询）';
COMMENT
ON COLUMN "public"."chat_turn"."reasoning_summary" IS '受控推理摘要: {"intent":"...","actions":[...],"outcome":"..."}（不包含模型原始思维链）';
COMMENT
ON COLUMN "public"."chat_turn"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."chat_turn"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."chat_turn"."status" IS '状态: ON/DISABLE';
COMMENT
ON COLUMN "public"."chat_turn"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."chat_turn"."remark" IS '备注';
COMMENT
ON TABLE "public"."chat_turn" IS '对话轮次';

-- ----------------------------
-- 2. execution_event（执行事件）
-- 用途: 替代前端独立 Timeline 数据源，按 turnId 聚合为 AI 回复的内嵌轨迹
-- ----------------------------
DROP TABLE IF EXISTS "public"."execution_event";
CREATE TABLE "public"."execution_event"
(
    "id"                  varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "turn_id"             varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "task_id"             varchar(64) COLLATE "pg_catalog"."default",
    "task_detail_id"      varchar(64) COLLATE "pg_catalog"."default",
    "event_type"          varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "step_name"           varchar(255) COLLATE "pg_catalog"."default",
    "command_name"        varchar(255) COLLATE "pg_catalog"."default",
    "command_content"     text COLLATE "pg_catalog"."default",
    "response_content"    text COLLATE "pg_catalog"."default",
    "failure_reason"      text COLLATE "pg_catalog"."default",
    "sequence_no"         int4                                        NOT NULL,
    "started_at"          timestamp(6),
    "finished_at"         timestamp(6),
    "atomic_command_id"   varchar(64) COLLATE "pg_catalog"."default",
    "atomic_command_code" varchar(128) COLLATE "pg_catalog"."default",
    "provider_id"         varchar(64) COLLATE "pg_catalog"."default",
    "provider_name"       varchar(128) COLLATE "pg_catalog"."default",
    "model_id"            varchar(64) COLLATE "pg_catalog"."default",
    "model_code"          varchar(128) COLLATE "pg_catalog"."default",
    "create_time"         timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"              varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying
)
;
COMMENT
ON COLUMN "public"."execution_event"."id" IS '事件主键，UUID';
COMMENT
ON COLUMN "public"."execution_event"."turn_id" IS '轮次主键，关联 chat_turn.id';
COMMENT
ON COLUMN "public"."execution_event"."task_id" IS '调度任务主键，关联 task.id';
COMMENT
ON COLUMN "public"."execution_event"."task_detail_id" IS '任务详情主键，关联 task_detail.id';
COMMENT
ON COLUMN "public"."execution_event"."event_type" IS '事件类型: CONTEXT_ASSEMBLING/CONTEXT_ASSEMBLED/MEMORY_MATCHING/MEMORY_MATCHED/MEMORY_MISSED/ATOMIC_COMMAND_START/ATOMIC_COMMAND_COMPLETE/ATOMIC_COMMAND_FAILED/AI_STARTED/AI_COMPLETED/SUB_AGENT_STARTED/SUB_AGENT_COMPLETED/TURN_COMPLETED/TASK_FAILED';
COMMENT
ON COLUMN "public"."execution_event"."step_name" IS '步骤名称（展示用）';
COMMENT
ON COLUMN "public"."execution_event"."command_name" IS '原子命令名称';
COMMENT
ON COLUMN "public"."execution_event"."command_content" IS '原子命令请求内容（截断500字符）';
COMMENT
ON COLUMN "public"."execution_event"."response_content" IS '原子命令响应内容（截断500字符，完整内容在 task_detail.return_params）';
COMMENT
ON COLUMN "public"."execution_event"."failure_reason" IS '失败原因';
COMMENT
ON COLUMN "public"."execution_event"."sequence_no" IS '轮次内事件序号，从1递增';
COMMENT
ON COLUMN "public"."execution_event"."started_at" IS '开始时间';
COMMENT
ON COLUMN "public"."execution_event"."finished_at" IS '结束时间';
COMMENT
ON COLUMN "public"."execution_event"."atomic_command_id" IS '原子命令主键';
COMMENT
ON COLUMN "public"."execution_event"."atomic_command_code" IS '原子命令编码';
COMMENT
ON COLUMN "public"."execution_event"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."execution_event"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."execution_event"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."execution_event"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."execution_event"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."execution_event"."status" IS '状态: ON/DISABLE';
COMMENT
ON TABLE "public"."execution_event" IS '执行事件';

-- ----------------------------
-- 3. memory_evidence（记忆证据）
-- 用途: 记忆提炼的数据来源，关联轮次与记忆版本
-- ----------------------------
DROP TABLE IF EXISTS "public"."memory_evidence";
CREATE TABLE "public"."memory_evidence"
(
    "id"                varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "turn_id"           varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "memory_version_id" varchar(64) COLLATE "pg_catalog"."default",
    "evidence_type"     varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "evidence_content"  jsonb                                       NOT NULL,
    "quality_score"     numeric(3, 2),
    "create_time"       timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"            varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying
)
;
COMMENT
ON COLUMN "public"."memory_evidence"."id" IS '证据主键，UUID';
COMMENT
ON COLUMN "public"."memory_evidence"."turn_id" IS '轮次主键，关联 chat_turn.id';
COMMENT
ON COLUMN "public"."memory_evidence"."memory_version_id" IS '记忆版本主键，关联 agent_memory_version.id';
COMMENT
ON COLUMN "public"."memory_evidence"."evidence_type" IS '证据类型: EXECUTION_TRACE/REASONING_SUMMARY';
COMMENT
ON COLUMN "public"."memory_evidence"."evidence_content" IS '证据内容: 原子命令调用链+结果摘要';
COMMENT
ON COLUMN "public"."memory_evidence"."quality_score" IS '质量评分 0.00-1.00';
COMMENT
ON COLUMN "public"."memory_evidence"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."memory_evidence"."status" IS '状态: ON/DISABLE';
COMMENT
ON TABLE "public"."memory_evidence" IS '记忆证据';

-- ============================================================
-- 第二部分：新表索引
-- ============================================================

-- chat_turn 索引
CREATE INDEX "idx_chat_turn_session" ON "public"."chat_turn" USING btree (
    "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_chat_turn_task" ON "public"."chat_turn" USING btree (
    "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_chat_turn_session_turn" ON "public"."chat_turn" USING btree (
    "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "turn_number" "pg_catalog"."int4_ops" ASC NULLS LAST
    );

-- execution_event 索引
CREATE INDEX "idx_execution_event_turn" ON "public"."execution_event" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_task" ON "public"."execution_event" USING btree (
    "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_type" ON "public"."execution_event" USING btree (
    "event_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_turn_seq" ON "public"."execution_event" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "sequence_no" "pg_catalog"."int4_ops" ASC NULLS LAST
    );

-- memory_evidence 索引
CREATE INDEX "idx_memory_evidence_turn" ON "public"."memory_evidence" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_memory_evidence_version" ON "public"."memory_evidence" USING btree (
    "memory_version_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ============================================================
-- 第三部分：新表主键约束
-- ============================================================

ALTER TABLE "public"."chat_turn"
    ADD CONSTRAINT "chat_turn_pkey" PRIMARY KEY ("id");
ALTER TABLE "public"."execution_event"
    ADD CONSTRAINT "execution_event_pkey" PRIMARY KEY ("id");
ALTER TABLE "public"."memory_evidence"
    ADD CONSTRAINT "memory_evidence_pkey" PRIMARY KEY ("id");

-- ============================================================
-- 第四部分：现有表 ALTER — 新增字段
-- ============================================================

-- agent_chat_session: 新增 create_user_id（创建者用户ID，用于归属校验）
ALTER TABLE "public"."agent_chat_session"
    ADD COLUMN IF NOT EXISTS "create_user_id" varchar (64) COLLATE "pg_catalog"."default";
COMMENT
ON COLUMN "public"."agent_chat_session"."create_user_id" IS '创建者用户ID，用于归属校验';

-- agent_chat_message: 新增 turn_id（关联 chat_turn）
ALTER TABLE "public"."agent_chat_message"
    ADD COLUMN IF NOT EXISTS "turn_id" varchar (64) COLLATE "pg_catalog"."default";
COMMENT
ON COLUMN "public"."agent_chat_message"."turn_id" IS '轮次主键，关联 chat_turn.id';

-- agent_chat_message: 新建 turn_id 索引
CREATE INDEX IF NOT EXISTS "idx_agent_chat_message_turn" ON "public"."agent_chat_message" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ============================================================
-- 第五部分：修复拼写错误 — reserver → reserve
-- 说明: 开发期不兼容，直接 RENAME COLUMN
-- ============================================================

-- agent_chat_message
ALTER TABLE "public"."agent_chat_message" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_chat_message"."reserve" IS '扩展字段，JSON格式';

-- agent_chat_session
ALTER TABLE "public"."agent_chat_session" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_chat_session"."reserve" IS '扩展字段，JSON格式';

-- agent_definition
ALTER TABLE "public"."agent_definition" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_definition"."reserve" IS '扩展字段，JSON格式';

-- agent_memory
ALTER TABLE "public"."agent_memory" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_memory"."reserve" IS '扩展字段，JSON格式';

-- agent_memory_detail
ALTER TABLE "public"."agent_memory_detail" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_memory_detail"."reserve" IS '扩展字段，JSON格式';

-- agent_rule
ALTER TABLE "public"."agent_rule" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_rule"."reserve" IS '扩展字段，JSON格式';

-- agent_skill
ALTER TABLE "public"."agent_skill" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."agent_skill"."reserve" IS '扩展字段，JSON格式';

-- ai_model
ALTER TABLE "public"."ai_model" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."ai_model"."reserve" IS '扩展字段，JSON格式';

-- ai_model_provider
ALTER TABLE "public"."ai_model_provider" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."ai_model_provider"."reserve" IS '扩展字段，JSON格式';

-- ai_user
ALTER TABLE "public"."ai_user" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."ai_user"."reserve" IS '扩展字段，JSON格式';

-- atomic_command
ALTER TABLE "public"."atomic_command" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."atomic_command"."reserve" IS '扩展字段，JSON格式';

-- sub_agent_relation
ALTER TABLE "public"."sub_agent_relation" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."sub_agent_relation"."reserve" IS '扩展字段，JSON格式';

-- task
ALTER TABLE "public"."task" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."task"."reserve" IS '扩展字段，JSON格式';

-- task_detail
ALTER TABLE "public"."task_detail" RENAME COLUMN "reserver" TO "reserve";
COMMENT
ON COLUMN "public"."task_detail"."reserve" IS '扩展字段，JSON格式';

-- ============================================================
-- 第六部分：补齐缺失的字段注释
-- ============================================================

-- agent_chat_message 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_chat_message"."id" IS '消息主键';
COMMENT
ON COLUMN "public"."agent_chat_message"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_chat_message"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_chat_message"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."agent_chat_message"."sequence_no" IS '会话内消息序号，从1递增';
COMMENT
ON COLUMN "public"."agent_chat_message"."remark" IS '备注';

-- agent_chat_session 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_chat_session"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."status" IS '状态: ON(启用) / OFF(停用)';

-- agent_client 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_client"."id" IS '主键，服务端分配的客户端唯一标识，也是WebSocket cliKey';
COMMENT
ON COLUMN "public"."agent_client"."status" IS '客户端状态: 1-ACTIVE(活跃) / 0-EXPIRED(已过期)';

-- agent_definition 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_definition"."default_model_id" IS '默认模型主键，关联 ai_model.id';
COMMENT
ON COLUMN "public"."agent_definition"."create_by" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."agent_definition"."update_by" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."agent_definition"."model" IS '模型（已废弃，由 default_model_id 替代）';

-- agent_memory 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_memory"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_memory"."update_time" IS '修改时间';

-- agent_memory_detail 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_memory_detail"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_memory_detail"."update_time" IS '修改时间';

-- agent_rule 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_rule"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_rule"."update_time" IS '修改时间';

-- agent_skill 缺失注释补齐
COMMENT
ON COLUMN "public"."agent_skill"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_skill"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_skill"."user_id" IS '用户归属ID，确保每个用户的技能私域隔离';
COMMENT
ON COLUMN "public"."agent_skill"."plan_output_schema" IS 'AI输出计划的结构规范(Schema)，替代旧的 return_data_format';
COMMENT
ON COLUMN "public"."agent_skill"."observation_schema" IS '执行结果观察格式规范(Schema)，定义执行器返回数据如何进入下一轮AI观察';

-- ai_model 缺失注释补齐
COMMENT
ON COLUMN "public"."ai_model"."id" IS '主键';
COMMENT
ON COLUMN "public"."ai_model"."provider_id" IS 'AI模型供应商主键，关联 ai_model_provider.id';
COMMENT
ON COLUMN "public"."ai_model"."model_code" IS '模型编码，同一供应商下唯一';
COMMENT
ON COLUMN "public"."ai_model"."model_name" IS '模型名称，用户可读';
COMMENT
ON COLUMN "public"."ai_model"."context_window" IS '上下文窗口大小（token数）';
COMMENT
ON COLUMN "public"."ai_model"."create_by" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."ai_model"."update_by" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."ai_model"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."ai_model"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."ai_model"."status" IS '状态: ON(启用) / OFF(停用)';

-- ai_model_provider 缺失注释补齐
COMMENT
ON COLUMN "public"."ai_model_provider"."id" IS '主键';
COMMENT
ON COLUMN "public"."ai_model_provider"."provider_code" IS '供应商编码，全局唯一';
COMMENT
ON COLUMN "public"."ai_model_provider"."provider_name" IS '供应商名称，用户可读';
COMMENT
ON COLUMN "public"."ai_model_provider"."base_url" IS 'API 基础地址';
COMMENT
ON COLUMN "public"."ai_model_provider"."timeout_millis" IS '请求超时时间（毫秒），默认60000';
COMMENT
ON COLUMN "public"."ai_model_provider"."create_by" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."ai_model_provider"."update_by" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."ai_model_provider"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."ai_model_provider"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."ai_model_provider"."status" IS '状态: ON(启用) / OFF(停用)';

-- ai_user 缺失注释补齐
COMMENT
ON COLUMN "public"."ai_user"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."ai_user"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."ai_user"."status" IS '状态: ON(启用) / OFF(停用)';

-- atomic_command 缺失注释补齐
COMMENT
ON COLUMN "public"."atomic_command"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."atomic_command"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."atomic_command"."status" IS '状态: ON(启用) / OFF(停用)';

-- sub_agent_relation 缺失注释补齐
COMMENT
ON COLUMN "public"."sub_agent_relation"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."sub_agent_relation"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."sub_agent_relation"."status" IS '状态: ON(启用) / OFF(停用)';

-- task 缺失注释补齐
COMMENT
ON COLUMN "public"."task"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."task"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."task"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."task"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."task"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."task"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."task"."status" IS '状态: ON(启用) / OFF(停用)';

-- task_detail 缺失注释补齐
COMMENT
ON COLUMN "public"."task_detail"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."task_detail"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."task_detail"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."task_detail"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."task_detail"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."task_detail"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."task_detail"."status" IS '状态: ON(启用) / OFF(停用)';

-- ============================================================
-- 第七部分：删除废弃字段
-- 说明: agent_definition.model 字段已被 default_model_id 替代
-- ============================================================

ALTER TABLE "public"."agent_definition" DROP COLUMN IF EXISTS "model";

-- ============================================================
-- 第八部分：agent_chat_message 新增 turn_id 索引（已在第四部分创建）
-- 补充 agent_chat_session 新增 create_user_id 索引
-- ============================================================

CREATE INDEX IF NOT EXISTS "idx_agent_chat_session_create_user" ON "public"."agent_chat_session" USING btree (
    "create_user_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ============================================================
-- 迁移完成
-- ============================================================
