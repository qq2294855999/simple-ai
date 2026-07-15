/*
 Navicat Premium Dump SQL

 Source Server         : 虚拟机
 Source Server Type    : PostgreSQL
 Source Server Version : 140018 (140018)
 Source Host           : develop.dev.joyswon.com:5432
 Source Catalog        : simple-ai
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 140018 (140018)
 File Encoding         : 65001

 Date: 16/07/2026 01:19:47
*/


-- ----------------------------
-- Table structure for agent_chat_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_message";
CREATE TABLE "public"."agent_chat_message" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "session_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "role" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "content_format" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "sequence_no" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_chat_message"."session_id" IS '聊天会话主键';
COMMENT ON COLUMN "public"."agent_chat_message"."task_id" IS '关联调度任务主键';
COMMENT ON COLUMN "public"."agent_chat_message"."role" IS '消息角色：USER、ASSISTANT、SYSTEM_ERROR';
COMMENT ON COLUMN "public"."agent_chat_message"."content" IS '消息内容';
COMMENT ON COLUMN "public"."agent_chat_message"."content_format" IS '内容格式：PLAIN_TEXT、RESTRICTED_MARKDOWN';
COMMENT ON TABLE "public"."agent_chat_message" IS '智能体聊天消息';

-- ----------------------------
-- Table structure for agent_chat_session
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_session";
CREATE TABLE "public"."agent_chat_session" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "session_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "last_message_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_chat_session"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_chat_session"."agent_id" IS '绑定智能体主键';
COMMENT ON COLUMN "public"."agent_chat_session"."session_name" IS '会话名称';
COMMENT ON COLUMN "public"."agent_chat_session"."last_message_at" IS '最后消息时间';
COMMENT ON TABLE "public"."agent_chat_session" IS '智能体聊天会话';

-- ----------------------------
-- Table structure for agent_definition
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_definition";
CREATE TABLE "public"."agent_definition" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "first_principle" text COLLATE "pg_catalog"."default",
  "second_rule" text COLLATE "pg_catalog"."default",
  "third_skill" text COLLATE "pg_catalog"."default",
  "model" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "default_model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_definition"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_definition"."name" IS '名称';
COMMENT ON COLUMN "public"."agent_definition"."definition_desc" IS '定义描述';
COMMENT ON COLUMN "public"."agent_definition"."first_principle" IS '第一铁律';
COMMENT ON COLUMN "public"."agent_definition"."second_rule" IS '第二规则';
COMMENT ON COLUMN "public"."agent_definition"."third_skill" IS '第三技能';
COMMENT ON COLUMN "public"."agent_definition"."model" IS '模型';
COMMENT ON COLUMN "public"."agent_definition"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."agent_definition"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."agent_definition"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_definition"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_definition"."status" IS '状态';
COMMENT ON COLUMN "public"."agent_definition"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."agent_definition"."remark" IS '备注';
COMMENT ON TABLE "public"."agent_definition" IS '智能体定义';

-- ----------------------------
-- Table structure for agent_memory
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory";
CREATE TABLE "public"."agent_memory" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "memory_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "trigger_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_action" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_memory"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_memory"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."agent_memory"."memory_name" IS '记忆名称';
COMMENT ON COLUMN "public"."agent_memory"."step_name" IS '步骤名称';
COMMENT ON COLUMN "public"."agent_memory"."trigger_condition" IS '触发条件';
COMMENT ON COLUMN "public"."agent_memory"."trigger_action" IS '触发动作';
COMMENT ON COLUMN "public"."agent_memory"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_memory"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_memory"."status" IS '状态';
COMMENT ON COLUMN "public"."agent_memory"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."agent_memory"."remark" IS '备注';
COMMENT ON TABLE "public"."agent_memory" IS '智能体记忆';

-- ----------------------------
-- Table structure for agent_memory_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory_detail";
CREATE TABLE "public"."agent_memory_detail" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_memory_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "exec_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_data_format" text COLLATE "pg_catalog"."default" NOT NULL,
  "parent_step_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "next_step_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "model" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_memory_detail"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_memory_detail"."agent_memory_id" IS '智能体记忆ID';
COMMENT ON COLUMN "public"."agent_memory_detail"."step_name" IS '步骤名称';
COMMENT ON COLUMN "public"."agent_memory_detail"."step_type" IS '步骤类型：智能体记忆步骤类型';
COMMENT ON COLUMN "public"."agent_memory_detail"."exec_content" IS '执行内容';
COMMENT ON COLUMN "public"."agent_memory_detail"."return_data_format" IS '返回的数据格式';
COMMENT ON COLUMN "public"."agent_memory_detail"."parent_step_id" IS '父步骤ID';
COMMENT ON COLUMN "public"."agent_memory_detail"."next_step_id" IS '下一个步骤ID';
COMMENT ON COLUMN "public"."agent_memory_detail"."branch_condition" IS '分支条件';
COMMENT ON COLUMN "public"."agent_memory_detail"."branch_route" IS '分支路由';
COMMENT ON COLUMN "public"."agent_memory_detail"."model" IS '模型';
COMMENT ON COLUMN "public"."agent_memory_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_memory_detail"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_memory_detail"."status" IS '状态';
COMMENT ON COLUMN "public"."agent_memory_detail"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."agent_memory_detail"."remark" IS '备注';
COMMENT ON TABLE "public"."agent_memory_detail" IS '智能体记忆详情';

-- ----------------------------
-- Table structure for agent_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_rule";
CREATE TABLE "public"."agent_rule" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_action" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_rule"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."agent_rule"."definition_desc" IS '定义描述';
COMMENT ON COLUMN "public"."agent_rule"."trigger_condition" IS '触发条件';
COMMENT ON COLUMN "public"."agent_rule"."trigger_action" IS '触发动作';
COMMENT ON COLUMN "public"."agent_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_rule"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_rule"."status" IS '状态';
COMMENT ON COLUMN "public"."agent_rule"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."agent_rule"."remark" IS '备注';
COMMENT ON TABLE "public"."agent_rule" IS '智能体规则';

-- ----------------------------
-- Table structure for agent_skill
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_skill";
CREATE TABLE "public"."agent_skill" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "exec_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_data_format" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_skill"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_skill"."agent_id" IS '智能体ID';
COMMENT ON COLUMN "public"."agent_skill"."definition_desc" IS '定义描述';
COMMENT ON COLUMN "public"."agent_skill"."exec_content" IS '执行内容';
COMMENT ON COLUMN "public"."agent_skill"."return_data_format" IS '返回的数据格式';
COMMENT ON COLUMN "public"."agent_skill"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_skill"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_skill"."status" IS '状态';
COMMENT ON COLUMN "public"."agent_skill"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."agent_skill"."remark" IS '备注';
COMMENT ON TABLE "public"."agent_skill" IS '智能体技能';

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_model";
CREATE TABLE "public"."ai_model" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "model_code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "model_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "capability_config" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::text,
  "context_window" int4,
  "provider_default" int2 NOT NULL DEFAULT 0,
  "system_default" int2 NOT NULL DEFAULT 0,
  "create_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."ai_model"."capability_config" IS '可扩展能力JSON文本，例如chat、vision、functionCalling';
COMMENT ON COLUMN "public"."ai_model"."provider_default" IS '供应商默认模型';
COMMENT ON COLUMN "public"."ai_model"."system_default" IS '系统默认模型，全局仅允许一个启用模型';
COMMENT ON TABLE "public"."ai_model" IS 'AI模型配置';

-- ----------------------------
-- Table structure for ai_model_provider
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_model_provider";
CREATE TABLE "public"."ai_model_provider" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "provider_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "protocol_type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "base_url" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "api_key_ciphertext" text COLLATE "pg_catalog"."default" NOT NULL,
  "timeout_millis" int4 NOT NULL DEFAULT 60000,
  "system_default" int2 NOT NULL DEFAULT 0,
  "create_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."ai_model_provider"."protocol_type" IS '协议类型，首期仅支持OPENAI_COMPATIBLE';
COMMENT ON COLUMN "public"."ai_model_provider"."api_key_ciphertext" IS 'API Key AES-GCM加密密文，禁止回显、日志与审计复制';
COMMENT ON COLUMN "public"."ai_model_provider"."system_default" IS '是否系统默认供应商，仅辅助运维展示；实际默认由模型表确定';
COMMENT ON TABLE "public"."ai_model_provider" IS 'AI模型供应商运行配置';

-- ----------------------------
-- Table structure for atomic_command
-- ----------------------------
DROP TABLE IF EXISTS "public"."atomic_command";
CREATE TABLE "public"."atomic_command" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "command" text COLLATE "pg_catalog"."default" NOT NULL,
  "role" text COLLATE "pg_catalog"."default" NOT NULL,
  "skill_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."atomic_command"."id" IS '主键';
COMMENT ON COLUMN "public"."atomic_command"."name" IS '名称';
COMMENT ON COLUMN "public"."atomic_command"."command" IS '命令';
COMMENT ON COLUMN "public"."atomic_command"."role" IS '作用';
COMMENT ON COLUMN "public"."atomic_command"."skill_id" IS '智能体技能ID';
COMMENT ON COLUMN "public"."atomic_command"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atomic_command"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."atomic_command"."status" IS '状态';
COMMENT ON COLUMN "public"."atomic_command"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."atomic_command"."remark" IS '备注';
COMMENT ON TABLE "public"."atomic_command" IS '原子命令';

-- ----------------------------
-- Table structure for sub_agent_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."sub_agent_relation";
CREATE TABLE "public"."sub_agent_relation" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "main_agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "sub_agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."sub_agent_relation"."id" IS '主键';
COMMENT ON COLUMN "public"."sub_agent_relation"."main_agent_id" IS '主智能体';
COMMENT ON COLUMN "public"."sub_agent_relation"."sub_agent_id" IS '子智能体';
COMMENT ON COLUMN "public"."sub_agent_relation"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sub_agent_relation"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."sub_agent_relation"."status" IS '状态';
COMMENT ON COLUMN "public"."sub_agent_relation"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."sub_agent_relation"."remark" IS '备注';
COMMENT ON TABLE "public"."sub_agent_relation" IS '子智能体关联';

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS "public"."task";
CREATE TABLE "public"."task" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_memory_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "agent_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "parent_task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "next_task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "exec_status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "failure_reason" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::text,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."task"."id" IS '主键';
COMMENT ON COLUMN "public"."task"."agent_memory_id" IS '智能体记忆主键';
COMMENT ON COLUMN "public"."task"."agent_id" IS '智能体主键';
COMMENT ON COLUMN "public"."task"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."task"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."task"."next_task_id" IS '下一个任务ID';
COMMENT ON COLUMN "public"."task"."step_type" IS '步骤类型：智能体步骤类型';
COMMENT ON COLUMN "public"."task"."branch_condition" IS '分支条件';
COMMENT ON COLUMN "public"."task"."branch_route" IS '分支路由';
COMMENT ON COLUMN "public"."task"."request_params" IS '请求参数';
COMMENT ON COLUMN "public"."task"."return_params" IS '返回参数';
COMMENT ON COLUMN "public"."task"."exec_status" IS '执行状态';
COMMENT ON COLUMN "public"."task"."failure_reason" IS '失败原因';
COMMENT ON COLUMN "public"."task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."task"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."task"."status" IS '状态';
COMMENT ON COLUMN "public"."task"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."task"."remark" IS '备注';
COMMENT ON TABLE "public"."task" IS '任务';

-- ----------------------------
-- Table structure for task_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."task_detail";
CREATE TABLE "public"."task_detail" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "parent_task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "next_task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "exec_status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."task_detail"."id" IS '主键';
COMMENT ON COLUMN "public"."task_detail"."task_id" IS '任务主键';
COMMENT ON COLUMN "public"."task_detail"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."task_detail"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."task_detail"."next_task_id" IS '下一个任务ID';
COMMENT ON COLUMN "public"."task_detail"."step_type" IS '步骤类型：智能体步骤类型';
COMMENT ON COLUMN "public"."task_detail"."branch_condition" IS '分支条件';
COMMENT ON COLUMN "public"."task_detail"."branch_route" IS '分支路由';
COMMENT ON COLUMN "public"."task_detail"."request_params" IS '请求参数';
COMMENT ON COLUMN "public"."task_detail"."return_params" IS '返回参数';
COMMENT ON COLUMN "public"."task_detail"."exec_status" IS '执行状态';
COMMENT ON COLUMN "public"."task_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."task_detail"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."task_detail"."status" IS '状态';
COMMENT ON COLUMN "public"."task_detail"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."task_detail"."remark" IS '备注';
COMMENT ON TABLE "public"."task_detail" IS '任务详情';

-- ----------------------------
-- Indexes structure for table agent_chat_message
-- ----------------------------
CREATE INDEX "idx_agent_chat_message_model_snapshot" ON "public"."agent_chat_message" USING btree (
  "model_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "provider_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "create_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_agent_chat_message_session_sequence" ON "public"."agent_chat_message" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "sequence_no" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_agent_chat_message_task" ON "public"."agent_chat_message" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table agent_chat_message
-- ----------------------------
ALTER TABLE "public"."agent_chat_message" ADD CONSTRAINT "uk_agent_chat_message_session_sequence" UNIQUE ("session_id", "sequence_no");

-- ----------------------------
-- Primary Key structure for table agent_chat_message
-- ----------------------------
ALTER TABLE "public"."agent_chat_message" ADD CONSTRAINT "agent_chat_message_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_chat_session
-- ----------------------------
CREATE INDEX "idx_agent_chat_session_agent_last_message" ON "public"."agent_chat_session" USING btree (
  "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "last_message_at" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);

-- ----------------------------
-- Primary Key structure for table agent_chat_session
-- ----------------------------
ALTER TABLE "public"."agent_chat_session" ADD CONSTRAINT "agent_chat_session_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_definition
-- ----------------------------
CREATE INDEX "idx_agent_definition_default_model" ON "public"."agent_definition" USING btree (
  "default_model_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_definition
-- ----------------------------
ALTER TABLE "public"."agent_definition" ADD CONSTRAINT "agent_definition_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_memory
-- ----------------------------
CREATE INDEX "idx_agent_memory_agent_status" ON "public"."agent_memory" USING btree (
  "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_memory
-- ----------------------------
ALTER TABLE "public"."agent_memory" ADD CONSTRAINT "agent_memory_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_memory_detail
-- ----------------------------
CREATE INDEX "idx_agent_memory_detail_memory_status" ON "public"."agent_memory_detail" USING btree (
  "agent_memory_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_agent_memory_detail_next_step" ON "public"."agent_memory_detail" USING btree (
  "next_step_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_agent_memory_detail_parent_step" ON "public"."agent_memory_detail" USING btree (
  "parent_step_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_memory_detail
-- ----------------------------
ALTER TABLE "public"."agent_memory_detail" ADD CONSTRAINT "agent_memory_detail_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_rule
-- ----------------------------
CREATE INDEX "idx_agent_rule_agent_status" ON "public"."agent_rule" USING btree (
  "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_rule
-- ----------------------------
ALTER TABLE "public"."agent_rule" ADD CONSTRAINT "agent_rule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_skill
-- ----------------------------
CREATE INDEX "idx_agent_skill_agent_status" ON "public"."agent_skill" USING btree (
  "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_skill
-- ----------------------------
ALTER TABLE "public"."agent_skill" ADD CONSTRAINT "agent_skill_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ai_model
-- ----------------------------
CREATE INDEX "idx_ai_model_provider_status_default" ON "public"."ai_model" USING btree (
  "provider_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "provider_default" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "uk_ai_model_one_system_default" ON "public"."ai_model" USING btree (
  "system_default" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE system_default = 1;

-- ----------------------------
-- Uniques structure for table ai_model
-- ----------------------------
ALTER TABLE "public"."ai_model" ADD CONSTRAINT "uk_ai_model_provider_model" UNIQUE ("provider_id", "model_code");

-- ----------------------------
-- Primary Key structure for table ai_model
-- ----------------------------
ALTER TABLE "public"."ai_model" ADD CONSTRAINT "ai_model_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ai_model_provider
-- ----------------------------
CREATE INDEX "idx_ai_model_provider_status" ON "public"."ai_model_provider" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST,
  "provider_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table ai_model_provider
-- ----------------------------
ALTER TABLE "public"."ai_model_provider" ADD CONSTRAINT "uk_ai_model_provider_code" UNIQUE ("provider_code");

-- ----------------------------
-- Primary Key structure for table ai_model_provider
-- ----------------------------
ALTER TABLE "public"."ai_model_provider" ADD CONSTRAINT "ai_model_provider_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table atomic_command
-- ----------------------------
CREATE INDEX "idx_atomic_command_name_status" ON "public"."atomic_command" USING btree (
  "name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atomic_command_skill_status" ON "public"."atomic_command" USING btree (
  "skill_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atomic_command_status" ON "public"."atomic_command" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table atomic_command
-- ----------------------------
ALTER TABLE "public"."atomic_command" ADD CONSTRAINT "atomic_command_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sub_agent_relation
-- ----------------------------
CREATE INDEX "idx_sub_agent_relation_main_status" ON "public"."sub_agent_relation" USING btree (
  "main_agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sub_agent_relation
-- ----------------------------
ALTER TABLE "public"."sub_agent_relation" ADD CONSTRAINT "sub_agent_relation_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table task
-- ----------------------------
CREATE INDEX "idx_task_agent_update" ON "public"."task" USING btree (
  "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "update_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_task_memory_status" ON "public"."task" USING btree (
  "agent_memory_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "exec_status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_task_model_snapshot" ON "public"."task" USING btree (
  "model_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "provider_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "create_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_task_parent_status" ON "public"."task" USING btree (
  "parent_task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "exec_status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table task
-- ----------------------------
ALTER TABLE "public"."task" ADD CONSTRAINT "task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table task_detail
-- ----------------------------
CREATE INDEX "idx_task_detail_model_snapshot" ON "public"."task_detail" USING btree (
  "model_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "provider_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "create_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_task_detail_parent_next" ON "public"."task_detail" USING btree (
  "parent_task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "next_task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_task_detail_task_status" ON "public"."task_detail" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "exec_status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table task_detail
-- ----------------------------
ALTER TABLE "public"."task_detail" ADD CONSTRAINT "task_detail_pkey" PRIMARY KEY ("id");
