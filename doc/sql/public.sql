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

 Date: 22/07/2026 19:51:42
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
-- Records of agent_chat_message
-- ----------------------------
INSERT INTO "public"."agent_chat_message"
VALUES ('2079874707756457984', '2079874618833018880', '', 'USER', '控制微信，找到文件传输助手，发送消息  你好啊', 'PLAIN_TEXT', 1, '2026-07-22 18:22:27.189',
        '2026-07-22 18:22:27.189', 1, '', '用户聊天消息', '', '', '', '');
INSERT INTO "public"."agent_chat_message"
VALUES ('2079874723304742912', '2079874618833018880', '2079874707785818112', 'SYSTEM_ERROR', '执行器类型[win_rpa]不存在', 'PLAIN_TEXT', 2,
        '2026-07-22 18:22:30.896', '2026-07-22 18:22:30.896', 1, '', '智能体最终回复消息', '', '', '', '');
INSERT INTO "public"."agent_chat_message"
VALUES ('2079874915840073728', '2079874618833018880', '', 'USER', '任务断了 重试', 'PLAIN_TEXT', 3, '2026-07-22 18:23:16.8', '2026-07-22 18:23:16.8', 1, '',
        '用户聊天消息', '', '', '', '');
INSERT INTO "public"."agent_chat_message"
VALUES ('2079874938707419136', '2079874618833018880', '2079874915848462336', 'ASSISTANT', '抱歉，当前会话没有之前任务的上下文记录。我无法知道之前中断的是什么任务。

能否请你说明一下：

- **之前的任务目标**是什么？（例如：打开某个软件、操作某个窗口、执行某个流程等）

这样我可以从断点处重新开始执行。', 'RESTRICTED_MARKDOWN', 4, '2026-07-22 18:23:22.252', '2026-07-22 18:23:22.252', 1, '', '智能体最终回复消息',
        '2077379712738693120', '词元之河', '2077431632937414656', 'deepseek-v4-pro');
INSERT INTO "public"."agent_chat_message"
VALUES ('2079874975210446848', '2079874618833018880', '', 'USER', '控制微信，找到文件传输助手，发送消息  你好啊', 'PLAIN_TEXT', 5, '2026-07-22 18:23:30.955',
        '2026-07-22 18:23:30.955', 1, '', '用户聊天消息', '', '', '', '');
INSERT INTO "public"."agent_chat_message"
VALUES ('2079875397899821056', '2079874618833018880', '2079874975223029760', 'SYSTEM_ERROR', '执行器类型[win_rpa]不存在', 'PLAIN_TEXT', 6,
        '2026-07-22 18:25:11.732', '2026-07-22 18:25:11.732', 1, '', '智能体最终回复消息', '', '', '', '');

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
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."agent_chat_session"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_chat_session"."agent_id" IS '绑定智能体主键';
COMMENT ON COLUMN "public"."agent_chat_session"."session_name" IS '会话名称';
COMMENT ON COLUMN "public"."agent_chat_session"."last_message_at" IS '最后消息时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."user_id" IS '用户归属ID，确保会话归属到具体用户';
COMMENT ON TABLE "public"."agent_chat_session" IS '智能体聊天会话';

-- ----------------------------
-- Records of agent_chat_session
-- ----------------------------
INSERT INTO "public"."agent_chat_session"
VALUES ('2079874618833018880', '2079806936913846272', '控制微信，找到文件传输助手，发送消息 你好啊', '2026-07-22 18:25:11.733', '2026-07-22 18:22:05.988',
        '2026-07-22 18:22:05.988', 1, '', '智能体人机对话会话', NULL);

-- ----------------------------
-- Table structure for agent_client
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_client";
CREATE TABLE "public"."agent_client"
(
    "id"                   varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_id"              varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "executor_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "client_name"          varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "client_secret_hash"   varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
    "expire_time"          timestamp(6),
    "last_connected_at"    timestamp(6),
    "last_disconnected_at" timestamp(6),
    "last_handshake_error" text COLLATE "pg_catalog"."default",
    "agent_version"        varchar(64) COLLATE "pg_catalog"."default",
    "machine_name"         varchar(128) COLLATE "pg_catalog"."default",
    "create_user_id"       varchar(32) COLLATE "pg_catalog"."default",
    "create_user_name"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time"          timestamp(6),
    "update_user_id"       varchar(32) COLLATE "pg_catalog"."default",
    "update_user_name"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time"          timestamp(6),
    "reserve"              text COLLATE "pg_catalog"."default",
    "remark"               text COLLATE "pg_catalog"."default",
    "status"               int2 DEFAULT 1
)
;
COMMENT
ON COLUMN "public"."agent_client"."id" IS '主键，服务端分配的客户端唯一标识，也是WebSocket cliKey';
COMMENT
ON COLUMN "public"."agent_client"."user_id" IS 'OAuth用户ID，标识客户端归属于哪个用户';
COMMENT
ON COLUMN "public"."agent_client"."executor_id" IS '执行器类型外键，关联 agent_executor.id';
COMMENT
ON COLUMN "public"."agent_client"."client_name" IS '客户端名称，用户可读，如 办公室电脑、家用笔记本';
COMMENT
ON COLUMN "public"."agent_client"."client_secret_hash" IS '客户端密钥的BCrypt哈希值，仅创建或轮换时返回一次明文';
COMMENT
ON COLUMN "public"."agent_client"."expire_time" IS '过期时间，服务端根据创建时选择的数字+单位计算';
COMMENT
ON COLUMN "public"."agent_client"."last_connected_at" IS '最后成功握手连接时间';
COMMENT
ON COLUMN "public"."agent_client"."last_disconnected_at" IS '最后断开连接时间';
COMMENT
ON COLUMN "public"."agent_client"."last_handshake_error" IS '最近一次鉴权失败原因描述，不含密钥信息';
COMMENT
ON COLUMN "public"."agent_client"."agent_version" IS '执行器软件版本号，握手成功后上报';
COMMENT
ON COLUMN "public"."agent_client"."machine_name" IS '机器名称，握手成功后上报，便于用户识别';
COMMENT
ON COLUMN "public"."agent_client"."create_user_id" IS '创建人用户ID';
COMMENT
ON COLUMN "public"."agent_client"."create_user_name" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."agent_client"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_client"."update_user_id" IS '修改人用户ID';
COMMENT
ON COLUMN "public"."agent_client"."update_user_name" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."agent_client"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_client"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."agent_client"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_client"."status" IS '客户端状态';
COMMENT
ON TABLE "public"."agent_client" IS '客户端实例';

-- ----------------------------
-- Records of agent_client
-- ----------------------------
INSERT INTO "public"."agent_client"
VALUES ('2079805807278751744', '1', '2079542278239834112', '家用', '$2a$10$TpFvA2YsZjIvyLnUsBjXCeUoqXAJsGHhXj46YCancdL4K6lgMgq3W', '2027-07-22 13:48:39.963',
        '2026-07-22 18:20:56.494', NULL, NULL, NULL, NULL, '1', NULL, '2026-07-22 13:48:40.035', NULL, NULL, '2026-07-22 18:20:56.497', NULL, NULL, 1);

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
  "default_model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id" varchar(255) COLLATE "pg_catalog"."default"
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
COMMENT
ON COLUMN "public"."agent_definition"."user_id" IS '用户归属ID，确保每个用户的智能体私域隔离';
COMMENT ON TABLE "public"."agent_definition" IS '智能体定义';

-- ----------------------------
-- Records of agent_definition
-- ----------------------------
INSERT INTO "public"."agent_definition"
VALUES ('2079806936913846272', '软件控制',
        '你是一个win10上的控制软件，你的职责是根据用户下达的任务，通过执行器原子命令进行软件控制，直到达成目的。记住，不是一次性生成所有命令顺序，而是一个命令一个命令的执行，根据返回的数据结果，在决定执行哪一个命令',
        NULL, NULL, NULL, '', '', '', '2026-07-22 13:53:09.361', '2026-07-22 18:21:56.214', 1, NULL, '', '2077431632937414656', NULL);

-- ----------------------------
-- Table structure for agent_executor
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_executor";
CREATE TABLE "public"."agent_executor"
(
    "id"               varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "executor_code"    varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "executor_name"    varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "description"      text COLLATE "pg_catalog"."default",
    "create_user_id"   varchar(32) COLLATE "pg_catalog"."default",
    "create_user_name" varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6),
    "update_user_id"   varchar(32) COLLATE "pg_catalog"."default",
    "update_user_name" varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6),
    "reserve"          text COLLATE "pg_catalog"."default",
    "remark"           text COLLATE "pg_catalog"."default",
    "status"           int2                                        NOT NULL DEFAULT 1
)
;
COMMENT
ON COLUMN "public"."agent_executor"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_executor"."executor_code" IS '执行器编码，唯一标识执行器类型，如 WINDOWS_RPA';
COMMENT
ON COLUMN "public"."agent_executor"."executor_name" IS '执行器名称，用户可读，如 Windows RPA 执行器';
COMMENT
ON COLUMN "public"."agent_executor"."description" IS '执行器描述，说明该类型执行器的主要能力和适用范围';
COMMENT
ON COLUMN "public"."agent_executor"."create_user_id" IS '创建人用户ID';
COMMENT
ON COLUMN "public"."agent_executor"."create_user_name" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."agent_executor"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_executor"."update_user_id" IS '修改人用户ID';
COMMENT
ON COLUMN "public"."agent_executor"."update_user_name" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."agent_executor"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_executor"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."agent_executor"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_executor"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON TABLE "public"."agent_executor" IS '执行器类型';

-- ----------------------------
-- Records of agent_executor
-- ----------------------------
INSERT INTO "public"."agent_executor"
VALUES ('2079542278239834112', 'win_rpa', 'WinRPA执行器', 'Win10的RPA执行器', NULL, NULL, '2026-07-21 20:21:29.816', NULL, NULL, '2026-07-22 10:31:41.345',
        NULL, NULL, 1);

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
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id" varchar(255) COLLATE "pg_catalog"."default"
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
COMMENT
ON COLUMN "public"."agent_memory"."user_id" IS '用户归属ID，确保每个用户的记忆私域隔离';
COMMENT ON TABLE "public"."agent_memory" IS '智能体记忆';

-- ----------------------------
-- Records of agent_memory
-- ----------------------------

-- ----------------------------
-- Table structure for agent_memory_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory_detail";
CREATE TABLE "public"."agent_memory_detail" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "agent_memory_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "step_type" int2 NOT NULL DEFAULT 1,
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
-- Records of agent_memory_detail
-- ----------------------------

-- ----------------------------
-- Table structure for agent_memory_version
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory_version";
CREATE TABLE "public"."agent_memory_version"
(
    "id"                varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "memory_id"         varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "version_no"        int4                                       NOT NULL DEFAULT 1,
    "version_status"    int2                                       NOT NULL DEFAULT 1,
    "source_task_id"    varchar(32) COLLATE "pg_catalog"."default",
    "success_assertion" text COLLATE "pg_catalog"."default",
    "summary"           text COLLATE "pg_catalog"."default",
    "create_reason"     text COLLATE "pg_catalog"."default",
    "create_user_id"    varchar(32) COLLATE "pg_catalog"."default",
    "create_time"       timestamp(6),
    "update_time"       timestamp(6)
)
;
COMMENT
ON COLUMN "public"."agent_memory_version"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_memory_version"."memory_id" IS '关联的记忆主键，对应 agent_memory.id';
COMMENT
ON COLUMN "public"."agent_memory_version"."version_no" IS '版本号，同一记忆下递增，从1开始';
COMMENT
ON COLUMN "public"."agent_memory_version"."version_status" IS '版本状态: 1-DRAFT(草稿) / 2-PUBLISHED(已发布) / 3-RETIRED(已淘汰)';
COMMENT
ON COLUMN "public"."agent_memory_version"."source_task_id" IS '来源任务主键，记录该版本由哪个任务产生';
COMMENT
ON COLUMN "public"."agent_memory_version"."success_assertion" IS '成功判定规则，用于验证记忆执行是否成功的条件';
COMMENT
ON COLUMN "public"."agent_memory_version"."summary" IS '版本摘要，AI生成的该版本简要描述';
COMMENT
ON COLUMN "public"."agent_memory_version"."create_reason" IS '创建原因: AI_EXPLORATION(AI探索沉淀) / MEMORY_REVISE(失败修订)';
COMMENT
ON COLUMN "public"."agent_memory_version"."create_user_id" IS '创建人用户ID';
COMMENT
ON COLUMN "public"."agent_memory_version"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_memory_version"."update_time" IS '修改时间';
COMMENT
ON TABLE "public"."agent_memory_version" IS '记忆版本';

-- ----------------------------
-- Records of agent_memory_version
-- ----------------------------

-- ----------------------------
-- Table structure for agent_memory_version_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory_version_detail";
CREATE TABLE "public"."agent_memory_version_detail"
(
    "id"                  varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "version_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "sequence_no"         int4                                        NOT NULL,
    "atomic_command_id"   varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "atomic_command_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "args_template"       text COLLATE "pg_catalog"."default",
    "delay_min_ms"        int4                                                 DEFAULT 100,
    "delay_max_ms"        int4                                                 DEFAULT 500,
    "timeout_ms"          int4                                                 DEFAULT 30000,
    "idempotency_key"     varchar(256) COLLATE "pg_catalog"."default",
    "success_assertion"   text COLLATE "pg_catalog"."default",
    "failure_strategy"    varchar(32) COLLATE "pg_catalog"."default"           DEFAULT 'STOP'::character varying,
    "status"              varchar(16) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying,
    "create_time"         timestamp(6),
    "update_time"         timestamp(6)
)
;
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."version_id" IS '关联的记忆版本主键，对应 agent_memory_version.id';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."sequence_no" IS '步骤序号，同一版本内从10开始递增，决定执行顺序';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."atomic_command_id" IS '原子命令主键，对应 atomic_command.id';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."atomic_command_code" IS '原子命令编码，冗余字段，如 window.find';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."args_template" IS '参数模板JSON，运行时变量替换后传给执行器';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."delay_min_ms" IS '执行前随机延迟最小值（毫秒），默认100';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."delay_max_ms" IS '执行前随机延迟最大值（毫秒），默认500';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."timeout_ms" IS '命令超时时间（毫秒），默认30000';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."idempotency_key" IS '幂等键，同一任务+步骤序号固定值，防止重复执行';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."success_assertion" IS '成功断言规则，用于判断该步骤是否执行成功';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."failure_strategy" IS '失败处理策略: STOP(停止) / RETRY(重试) / SKIP(跳过)';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_memory_version_detail"."update_time" IS '修改时间';
COMMENT
ON TABLE "public"."agent_memory_version_detail" IS '记忆版本步骤';

-- ----------------------------
-- Records of agent_memory_version_detail
-- ----------------------------

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
-- Records of agent_rule
-- ----------------------------

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
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id" varchar(255) COLLATE "pg_catalog"."default",
  "plan_output_schema" text COLLATE "pg_catalog"."default",
  "observation_schema" text COLLATE "pg_catalog"."default"
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
COMMENT
ON COLUMN "public"."agent_skill"."user_id" IS '用户归属ID，确保每个用户的技能私域隔离';
COMMENT
ON COLUMN "public"."agent_skill"."plan_output_schema" IS 'AI输出计划的结构规范(Schema)，替代旧的 return_data_format';
COMMENT
ON COLUMN "public"."agent_skill"."observation_schema" IS '执行结果观察格式规范(Schema)，定义执行器返回数据如何进入下一轮AI观察';
COMMENT ON TABLE "public"."agent_skill" IS '智能体技能';

-- ----------------------------
-- Records of agent_skill
-- ----------------------------

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
-- Records of ai_model
-- ----------------------------
INSERT INTO "public"."ai_model"
VALUES ('2077431632937414656', '2077379712738693120', 'deepseek-v4-pro', 'deepseek-v4-pro', '', NULL, 1, 1, '', '', '2026-07-16 00:34:32.762',
        '2026-07-16 00:34:32.763', 1, NULL, '');

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
-- Records of ai_model_provider
-- ----------------------------
INSERT INTO "public"."ai_model_provider"
VALUES ('2077379712738693120', 'cyzh', '词元之河', 'OPENAI_COMPATIBLE', 'https://api.tokenriver.cn/v1',
        'YffVNTsxKioJr0zRiZoA6cOGvjB7CAPpez8X5zFRKAaIUFZk753qx7CdJyMOsIJD3oUapjauPdYzZOM/OGZY3dNQTPry9nL2vJg=', 30000, 1, '', '', '2026-07-15 21:08:14.023',
        '2026-07-15 21:08:14.024', 1, NULL, '');

-- ----------------------------
-- Table structure for ai_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_user";
CREATE TABLE "public"."ai_user"
(
    "id"          varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "nickname"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "avatar_url"  varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "daily_quota" int4                                        NOT NULL DEFAULT 100,
    "used_quota"  int4                                        NOT NULL DEFAULT 0,
    "preferences" text COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"      int2                                        NOT NULL DEFAULT 1,
    "reserver"    text COLLATE "pg_catalog"."default",
    "remark"      varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT
ON COLUMN "public"."ai_user"."id" IS '主键，与授权中心sys_user.id一致';
COMMENT
ON COLUMN "public"."ai_user"."nickname" IS '用户昵称（冗余，减少跨服务查询）';
COMMENT
ON COLUMN "public"."ai_user"."avatar_url" IS '头像URL（冗余，减少跨服务查询）';
COMMENT
ON COLUMN "public"."ai_user"."daily_quota" IS '每日AI调用次数上限';
COMMENT
ON COLUMN "public"."ai_user"."used_quota" IS '当日已使用调用次数';
COMMENT
ON COLUMN "public"."ai_user"."preferences" IS '用户偏好JSON（语言、主题等）';
COMMENT
ON TABLE "public"."ai_user" IS 'AI平台用户';

-- ----------------------------
-- Records of ai_user
-- ----------------------------

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
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id" varchar(255) COLLATE "pg_catalog"."default",
  "executor_id" varchar(255) COLLATE "pg_catalog"."default"
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
COMMENT
ON COLUMN "public"."atomic_command"."user_id" IS '用户归属ID，确保每个用户的原子命令私域隔离';
COMMENT
ON COLUMN "public"."atomic_command"."executor_id" IS '执行器类型外键，关联 agent_executor.id，替代旧的 executor_type 字符串字段';
COMMENT ON TABLE "public"."atomic_command" IS '原子命令';

-- ----------------------------
-- Records of atomic_command
-- ----------------------------

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
-- Records of sub_agent_relation
-- ----------------------------

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
  "step_type"   int2                                        NOT NULL DEFAULT 1,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "exec_status" int2                                        NOT NULL DEFAULT 1,
  "failure_reason" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::text,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code"  varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "user_id"     varchar(255) COLLATE "pg_catalog"."default",
  "client_id"   varchar(255) COLLATE "pg_catalog"."default",
  "memory_version_id" varchar(255) COLLATE "pg_catalog"."default",
  "dispatch_id" varchar(255) COLLATE "pg_catalog"."default"
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
COMMENT
ON COLUMN "public"."task"."user_id" IS '用户归属ID，确保任务归属到具体用户';
COMMENT
ON COLUMN "public"."task"."client_id" IS '执行客户端主键，关联 agent_client.id，记录由哪个客户端执行';
COMMENT
ON COLUMN "public"."task"."memory_version_id" IS '记忆版本主键，关联 agent_memory_version.id，记录命中哪个记忆版本';
COMMENT
ON COLUMN "public"."task"."dispatch_id" IS '下发批次标识，服务端雪花ID，关联一次 WebSocket 批量命令下发';
COMMENT ON TABLE "public"."task" IS '任务';

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO "public"."task"
VALUES ('2079874707785818112', '', '2079806936913846272', '人机对话', '', '', 2, '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"控制微信，找到文件传输助手，发送消息  你好啊","clientId":"2079805807278751744","sessionId":"2079874618833018880","modelId":"2077431632937414656"}',
        '', 4, '执行器类型[win_rpa]不存在', '2026-07-22 18:22:27.196', '2026-07-22 18:22:27.196', 1, '', '智能体命令调度任务', '', '', '', '', NULL, NULL, NULL,
        NULL);
INSERT INTO "public"."task"
VALUES ('2079874915848462336', '', '2079806936913846272', '人机对话', '', '', 2, '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"任务断了 重试","clientId":"2079805807278751744","sessionId":"2079874618833018880","modelId":"2077431632937414656"}', '抱歉，当前会话没有之前任务的上下文记录。我无法知道之前中断的是什么任务。

能否请你说明一下：

- **之前的任务目标**是什么？（例如：打开某个软件、操作某个窗口、执行某个流程等）

这样我可以从断点处重新开始执行。', 3, '', '2026-07-22 18:23:16.802', '2026-07-22 18:23:16.802', 1, '', '智能体命令调度任务', '2077379712738693120', '词元之河',
        '2077431632937414656', 'deepseek-v4-pro', NULL, NULL, NULL, NULL);
INSERT INTO "public"."task"
VALUES ('2079874975223029760', '', '2079806936913846272', '人机对话', '', '', 2, '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"控制微信，找到文件传输助手，发送消息  你好啊","clientId":"2079805807278751744","sessionId":"2079874618833018880","modelId":"2077431632937414656"}',
        '', 4, '执行器类型[win_rpa]不存在', '2026-07-22 18:23:30.958', '2026-07-22 18:23:30.958', 1, '', '智能体命令调度任务', '', '', '', '', NULL, NULL, NULL,
        NULL);

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
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserver" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code"  varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "command_id"  varchar(255) COLLATE "pg_catalog"."default",
  "atomic_command_id" varchar(255) COLLATE "pg_catalog"."default",
  "client_id"   varchar(255) COLLATE "pg_catalog"."default",
  "sequence_no" int4,
  "dispatch_id" varchar(255) COLLATE "pg_catalog"."default",
  "step_type"   int2                                        NOT NULL DEFAULT 1,
  "exec_status" int2                                        NOT NULL DEFAULT 1
)
;
COMMENT ON COLUMN "public"."task_detail"."id" IS '主键';
COMMENT ON COLUMN "public"."task_detail"."task_id" IS '任务主键';
COMMENT ON COLUMN "public"."task_detail"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."task_detail"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."task_detail"."next_task_id" IS '下一个任务ID';
COMMENT ON COLUMN "public"."task_detail"."branch_condition" IS '分支条件';
COMMENT ON COLUMN "public"."task_detail"."branch_route" IS '分支路由';
COMMENT ON COLUMN "public"."task_detail"."request_params" IS '请求参数';
COMMENT ON COLUMN "public"."task_detail"."return_params" IS '返回参数';
COMMENT ON COLUMN "public"."task_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."task_detail"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."task_detail"."status" IS '状态';
COMMENT ON COLUMN "public"."task_detail"."reserver" IS '扩展';
COMMENT ON COLUMN "public"."task_detail"."remark" IS '备注';
COMMENT
ON COLUMN "public"."task_detail"."command_id" IS '单条命令标识，服务端雪花ID，用于匹配 WebSocket 回执';
COMMENT
ON COLUMN "public"."task_detail"."atomic_command_id" IS '原子命令主键，关联 atomic_command.id';
COMMENT
ON COLUMN "public"."task_detail"."client_id" IS '执行客户端主键，关联 agent_client.id';
COMMENT
ON COLUMN "public"."task_detail"."sequence_no" IS '步骤序号，同一任务内从10开始递增';
COMMENT
ON COLUMN "public"."task_detail"."dispatch_id" IS '下发批次标识，回显服务端的批次ID';
COMMENT
ON COLUMN "public"."task_detail"."step_type" IS '步骤类型：智能体步骤类型';
COMMENT
ON COLUMN "public"."task_detail"."exec_status" IS '执行状态';
COMMENT ON TABLE "public"."task_detail" IS '任务详情';

-- ----------------------------
-- Records of task_detail
-- ----------------------------
INSERT INTO "public"."task_detail"
VALUES ('2079874723275382784', '2079874707785818112', '人机对话', '', '', '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"控制微信，找到文件传输助手，发送消息  你好啊","clientId":"2079805807278751744","sessionId":"2079874618833018880","modelId":"2077431632937414656"}',
        '执行器类型[win_rpa]不存在', '2026-07-22 18:22:30.89', '2026-07-22 18:22:30.89', 1, '', '智能体命令调度失败详情', '', '', '', '', NULL, NULL, NULL,
        NULL, NULL, 1, 4);
INSERT INTO "public"."task_detail"
VALUES ('2079874938665476096', '2079874915848462336', '人机对话', '', '', '', '',
        '{"agentId":"2079806936913846272","modelId":"2077431632937414656","promptContent":"# 系统铁律\n必须优先保证用户目标闭环；必须遵守安全边界；必须记录每个任务步骤；必须在失败时返回明确失败原因；最终用户回复只能使用受限Markdown（标题、段落、列表、引用、表格、行内代码、带语言标识的代码块），禁止HTML、SVG、脚本、事件属性；调度过程必须通过结构化事件输出，不得伪装成最终回复。当用户要求创建记忆、规则、技能或智能体时，必须在任务步骤中使用WRITE角色并以JSON格式输出创建参数。创建记忆JSON格式：{\"type\":\"创建记忆\",\"agentId\":\"当前智能体ID\",\"memoryName\":\"记忆名称\",\"stepName\":\"步骤名称\",\"triggerCondition\":\"触发条件\",\"triggerAction\":\"触发动作\"}；创建规则JSON格式：{\"type\":\"创建规则\",\"agentId\":\"当前智能体ID\",\"definitionDesc\":\"定义描述\",\"triggerCondition\":\"触发条件\",\"triggerAction\":\"触发动作\"}；创建技能JSON格式：{\"type\":\"创建技能\",\"agentId\":\"当前智能体ID\",\"definitionDesc\":\"定义描述\",\"execContent\":\"执行内容\",\"returnDataFormat\":\"返回格式\"}；创建智能体JSON格式：{\"type\":\"创建智能体\",\"name\":\"名称\",\"definitionDesc\":\"定义描述\"}。\n\n# 智能体定义\n你是一个win10上的控制软件，你的职责是根据用户下达的任务，通过执行器原子命令进行软件控制，直到达成目的。记住，不是一次性生成所有命令顺序，而是一个命令一个命令的执行，根据返回的数据结果，在决定执行哪一个命令\n\n# 第一铁律\nnull\n\n# 第二规则\nnull\n\n# 第三技能\nnull\n\n# 直属规则\n\n# 直属技能\n\n# 子智能体关系\n\n# 候选记忆\n\n# 可用执行器类型\n## 执行器\n编码：win_rpa\n名称：WinRPA执行器\n描述：Win10的RPA执行器\n\n# 会话摘要\n\n","commandContent":"任务断了 重试"}',
        '{"success":true,"responseContent":"抱歉，当前会话没有之前任务的上下文记录。我无法知道之前中断的是什么任务。\n\n能否请你说明一下：\n\n- **之前的任务目标**是什么？（例如：打开某个软件、操作某个窗口、执行某个流程等）\n\n这样我可以从断点处重新开始执行。","failureReason":"","providerId":"2077379712738693120","providerName":"词元之河","modelId":"2077431632937414656","modelCode":"deepseek-v4-pro"}',
        '2026-07-22 18:23:22.242', '2026-07-22 18:23:22.242', 1, '', 'AI探索执行详情', '2077379712738693120', '词元之河', '2077431632937414656',
        'deepseek-v4-pro', NULL, NULL, NULL, NULL, NULL, 1, 3);
INSERT INTO "public"."task_detail"
VALUES ('2079875397866266624', '2079874975223029760', '人机对话', '', '', '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"控制微信，找到文件传输助手，发送消息  你好啊","clientId":"2079805807278751744","sessionId":"2079874618833018880","modelId":"2077431632937414656"}',
        '执行器类型[win_rpa]不存在', '2026-07-22 18:25:11.724', '2026-07-22 18:25:11.724', 1, '', '智能体命令调度失败详情', '', '', '', '', NULL, NULL, NULL,
        NULL, NULL, 1, 4);

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
-- Uniques structure for table agent_client
-- ----------------------------
ALTER TABLE "public"."agent_client"
    ADD CONSTRAINT "agent_client_user_id_client_name_key" UNIQUE ("user_id", "client_name");

-- ----------------------------
-- Primary Key structure for table agent_client
-- ----------------------------
ALTER TABLE "public"."agent_client"
    ADD CONSTRAINT "agent_client_pkey" PRIMARY KEY ("id");

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
-- Uniques structure for table agent_executor
-- ----------------------------
ALTER TABLE "public"."agent_executor"
    ADD CONSTRAINT "agent_executor_executor_code_key" UNIQUE ("executor_code");

-- ----------------------------
-- Primary Key structure for table agent_executor
-- ----------------------------
ALTER TABLE "public"."agent_executor"
    ADD CONSTRAINT "agent_executor_pkey" PRIMARY KEY ("id");

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
-- Uniques structure for table agent_memory_version
-- ----------------------------
ALTER TABLE "public"."agent_memory_version"
    ADD CONSTRAINT "agent_memory_version_memory_id_version_no_key" UNIQUE ("memory_id", "version_no");

-- ----------------------------
-- Primary Key structure for table agent_memory_version
-- ----------------------------
ALTER TABLE "public"."agent_memory_version"
    ADD CONSTRAINT "agent_memory_version_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_memory_version_detail
-- ----------------------------
CREATE INDEX "idx_mvd_version" ON "public"."agent_memory_version_detail" USING btree (
    "version_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "sequence_no" "pg_catalog"."int4_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table agent_memory_version_detail
-- ----------------------------
ALTER TABLE "public"."agent_memory_version_detail"
    ADD CONSTRAINT "agent_memory_version_detail_pkey" PRIMARY KEY ("id");

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
-- Primary Key structure for table ai_user
-- ----------------------------
ALTER TABLE "public"."ai_user"
    ADD CONSTRAINT "ai_user_pkey" PRIMARY KEY ("id");

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
    "exec_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_task_model_snapshot" ON "public"."task" USING btree (
  "model_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "provider_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "create_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_task_parent_status" ON "public"."task" USING btree (
  "parent_task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "exec_status" "pg_catalog"."int2_ops" ASC NULLS LAST
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

-- ----------------------------
-- Primary Key structure for table task_detail
-- ----------------------------
ALTER TABLE "public"."task_detail" ADD CONSTRAINT "task_detail_pkey" PRIMARY KEY ("id");
