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

 Date: 26/07/2026 11:34:00
*/


-- ----------------------------
-- Table structure for agent_chat_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_message";
CREATE TABLE "public"."agent_chat_message" (
                                               "id"          varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                               "session_id"  varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "task_id"     varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "role" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "content_format" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "sequence_no" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "provider_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "model_id"    varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "model_code" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "turn_id"     varchar(32) COLLATE "pg_catalog"."default"
)
;
COMMENT
ON COLUMN "public"."agent_chat_message"."id" IS '消息主键';
COMMENT ON COLUMN "public"."agent_chat_message"."session_id" IS '聊天会话主键';
COMMENT ON COLUMN "public"."agent_chat_message"."task_id" IS '关联调度任务主键';
COMMENT ON COLUMN "public"."agent_chat_message"."role" IS '消息角色：USER、ASSISTANT、SYSTEM_ERROR';
COMMENT ON COLUMN "public"."agent_chat_message"."content" IS '消息内容';
COMMENT ON COLUMN "public"."agent_chat_message"."content_format" IS '内容格式：PLAIN_TEXT、RESTRICTED_MARKDOWN';
COMMENT
ON COLUMN "public"."agent_chat_message"."sequence_no" IS '会话内消息序号，从1递增';
COMMENT
ON COLUMN "public"."agent_chat_message"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_chat_message"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_chat_message"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."agent_chat_message"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."agent_chat_message"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_chat_message"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."agent_chat_message"."turn_id" IS '轮次主键，关联 chat_turn.id';
COMMENT ON TABLE "public"."agent_chat_message" IS '智能体聊天消息';

-- ----------------------------
-- Records of agent_chat_message
-- ----------------------------
INSERT INTO "public"."agent_chat_message"
VALUES ('2080642571690094592', '2080642554497642496', '', 'USER', '打开微信', 'PLAIN_TEXT', 1, '2026-07-24 21:13:40.217', '2026-07-24 21:13:40.217', 1, '',
        '用户聊天消息', '', '', '', '', NULL);
INSERT INTO "public"."agent_chat_message"
VALUES ('2080642642179567616', '2080642554497642496', '2080642571761397760', 'SYSTEM_ERROR', '客户端实例[2079542278239834112]不存在', 'PLAIN_TEXT', 2,
        '2026-07-24 21:13:57.024', '2026-07-24 21:13:57.024', 1, '', '智能体最终回复消息', '', '', '', '', NULL);

-- ----------------------------
-- Table structure for agent_chat_session
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_session";
CREATE TABLE "public"."agent_chat_session" (
                                               "id"             varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                               "agent_id"       varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "session_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "last_message_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
                                               "reserve"        text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "user_id"        varchar(32) COLLATE "pg_catalog"."default",
                                               "create_user_id" varchar(32) COLLATE "pg_catalog"."default",
                                               "model_id"       varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "client_id"      varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."agent_chat_session"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_chat_session"."agent_id" IS '绑定智能体主键';
COMMENT ON COLUMN "public"."agent_chat_session"."session_name" IS '会话名称';
COMMENT ON COLUMN "public"."agent_chat_session"."last_message_at" IS '最后消息时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_chat_session"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."agent_chat_session"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."agent_chat_session"."user_id" IS '用户归属ID，确保会话归属到具体用户';
COMMENT
ON COLUMN "public"."agent_chat_session"."create_user_id" IS '创建者用户ID，用于归属校验';
COMMENT
ON COLUMN "public"."agent_chat_session"."model_id" IS '模型主键，会话级默认模型';
COMMENT
ON COLUMN "public"."agent_chat_session"."client_id" IS '客户端主键，会话级默认执行客户端';
COMMENT ON TABLE "public"."agent_chat_session" IS '智能体聊天会话';

-- ----------------------------
-- Records of agent_chat_session
-- ----------------------------
INSERT INTO "public"."agent_chat_session"
VALUES ('2080642554497642496', '2079806936913846272', '打开微信', '2026-07-24 21:13:57.024', '2026-07-24 21:13:36.118', '2026-07-24 21:13:36.118', 1, '',
        '智能体人机对话会话', '1', '1', '2077431632937414656', '2080625787096334336');

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
ON COLUMN "public"."agent_client"."status" IS '客户端状态: 1-ACTIVE(活跃) / 0-EXPIRED(已过期)';
COMMENT
ON TABLE "public"."agent_client" IS '客户端实例';

-- ----------------------------
-- Records of agent_client
-- ----------------------------
INSERT INTO "public"."agent_client"
VALUES ('2080625787096334336', '1', '2079542278239834112', '家用', '$2a$10$873vG3GE.lcpgdogj4NrIehQbjSL3pW3lo/rxuoeXr/pyK3JXzKPG', '2027-07-24 20:06:58.403',
        '2026-07-24 21:12:55.344', NULL, NULL, NULL, NULL, '1', NULL, '2026-07-24 20:06:58.458', NULL, NULL, '2026-07-24 21:12:55.348', NULL, NULL, 1);

-- ----------------------------
-- Table structure for agent_definition
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_definition";
CREATE TABLE "public"."agent_definition" (
                                             "id"               varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "first_principle" text COLLATE "pg_catalog"."default",
  "second_rule" text COLLATE "pg_catalog"."default",
  "third_skill" text COLLATE "pg_catalog"."default",
  "create_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                             "default_model_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                             "user_id"          varchar(32) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."agent_definition"."id" IS '主键';
COMMENT ON COLUMN "public"."agent_definition"."name" IS '名称';
COMMENT ON COLUMN "public"."agent_definition"."definition_desc" IS '定义描述';
COMMENT ON COLUMN "public"."agent_definition"."first_principle" IS '第一铁律';
COMMENT ON COLUMN "public"."agent_definition"."second_rule" IS '第二规则';
COMMENT ON COLUMN "public"."agent_definition"."third_skill" IS '第三技能';
COMMENT
ON COLUMN "public"."agent_definition"."create_by" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."agent_definition"."update_by" IS '修改人用户名称';
COMMENT ON COLUMN "public"."agent_definition"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_definition"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."agent_definition"."status" IS '状态';
COMMENT
ON COLUMN "public"."agent_definition"."reserve" IS '扩展字段，JSON格式';
COMMENT ON COLUMN "public"."agent_definition"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_definition"."default_model_id" IS '默认模型主键，关联 ai_model.id';
COMMENT
ON COLUMN "public"."agent_definition"."user_id" IS '用户归属ID，确保每个用户的智能体私域隔离';
COMMENT ON TABLE "public"."agent_definition" IS '智能体定义';

-- ----------------------------
-- Records of agent_definition
-- ----------------------------
INSERT INTO "public"."agent_definition"
VALUES ('2079806936913846272', '软件控制',
        '你是一个win10上的控制软件，你的职责是根据用户下达的任务，通过执行器原子命令进行软件控制，直到达成目的。记住，不是一次性生成所有命令顺序，而是一个命令一个命令的执行，根据返回的数据结果，在决定执行哪一个命令',
        NULL, NULL, NULL, '', '', '2026-07-22 13:53:09.361', '2026-07-22 18:21:56.214', 1, NULL, '', '2077431632937414656', NULL);

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
    "status"           int2                                        NOT NULL DEFAULT 1,
    "protocol_id"      varchar(32) COLLATE "pg_catalog"."default"
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
ON COLUMN "public"."agent_executor"."protocol_id" IS '协议外键，关联 agent_protocol.id，标识该执行器使用的对接协议';
COMMENT
ON TABLE "public"."agent_executor" IS '执行器类型';

-- ----------------------------
-- Records of agent_executor
-- ----------------------------
INSERT INTO "public"."agent_executor"
VALUES ('2079542278239834112', 'win_rpa', 'WinRPA执行器', 'Win10的RPA执行器', NULL, NULL, '2026-07-21 20:21:29.816', NULL, NULL, '2026-07-24 18:26:58.437',
        NULL, NULL, 1, '2080577692820090880');

-- ----------------------------
-- Table structure for agent_memory
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory";
CREATE TABLE "public"."agent_memory" (
                                         "id"                varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
                                         "agent_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                         "memory_name"       varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
                                         "remark"            varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                         "user_id"           varchar(32) COLLATE "pg_catalog"."default",
                                         "params_definition" jsonb,
                                         "version_no"        int4                                        NOT NULL DEFAULT 1,
                                         "version_status"    int2                                        NOT NULL DEFAULT 1,
                                         "source_task_id"    varchar(32) COLLATE "pg_catalog"."default"           DEFAULT ''::character varying,
                                         "summary"           text COLLATE "pg_catalog"."default"         NOT NULL DEFAULT ''::text,
                                         "create_reason"     varchar(64) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'MANUAL'::character varying,
                                         "client_id"         varchar(32) COLLATE "pg_catalog"."default"           DEFAULT ''::character varying,
                                         "create_user_id"    varchar(32) COLLATE "pg_catalog"."default"           DEFAULT ''::character varying,
                                         "parent_memory_id"  varchar(32) COLLATE "pg_catalog"."default"           DEFAULT ''::character varying
)
;
COMMENT
ON COLUMN "public"."agent_memory"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_memory"."agent_id" IS '智能体ID';
COMMENT
ON COLUMN "public"."agent_memory"."memory_name" IS '记忆名称模板，支持{param}占位符';
COMMENT ON COLUMN "public"."agent_memory"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."agent_memory"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_memory"."status" IS '状态';
COMMENT
ON COLUMN "public"."agent_memory"."reserve" IS '扩展字段，JSON格式';
COMMENT ON COLUMN "public"."agent_memory"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_memory"."user_id" IS '用户归属ID，确保每个用户的记忆私域隔离';
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
COMMENT
ON COLUMN "public"."agent_memory"."parent_memory_id" IS '父记忆ID，修订场景下指向被修订的旧版本记忆，首次探索沉淀时为空';
COMMENT ON TABLE "public"."agent_memory" IS '智能体记忆';

-- ----------------------------
-- Records of agent_memory
-- ----------------------------

-- ----------------------------
-- Table structure for agent_memory_step
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memory_step";
CREATE TABLE "public"."agent_memory_step"
(
    "id"                  varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "memory_id"           varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
    "sequence_no"         int4                                        NOT NULL,
    "atomic_command_id"   varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
    "atomic_command_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "step_name"           varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "args_template"       jsonb,
    "delay_min_ms"        int4                                                 DEFAULT 100,
    "delay_max_ms"        int4                                                 DEFAULT 500,
    "timeout_ms"          int4                                                 DEFAULT 30000,
    "success_assertion"   text COLLATE "pg_catalog"."default",
    "failure_strategy"    varchar(32) COLLATE "pg_catalog"."default"           DEFAULT 'STOP'::character varying,
    "status"              varchar(16) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT 'ON'::character varying,
    "create_time"         timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time"         timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
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

-- ----------------------------
-- Records of agent_memory_step
-- ----------------------------

-- ----------------------------
-- Table structure for agent_protocol
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_protocol";
CREATE TABLE "public"."agent_protocol"
(
    "id"               varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "protocol_code"    varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "protocol_name"    varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "protocol_version" varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "content"          text COLLATE "pg_catalog"."default"         NOT NULL,
    "create_user_id"   varchar(32) COLLATE "pg_catalog"."default",
    "create_user_name" varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6),
    "update_user_id"   varchar(32) COLLATE "pg_catalog"."default",
    "update_user_name" varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6),
    "reserve"          text COLLATE "pg_catalog"."default",
    "status"           int2                                        NOT NULL DEFAULT 1
)
;
COMMENT
ON COLUMN "public"."agent_protocol"."id" IS '主键';
COMMENT
ON COLUMN "public"."agent_protocol"."protocol_code" IS '协议编码，唯一标识协议类型，如 SEP_V1';
COMMENT
ON COLUMN "public"."agent_protocol"."protocol_name" IS '协议名称，用户可读，如 Simple Executor Protocol v1.0';
COMMENT
ON COLUMN "public"."agent_protocol"."protocol_version" IS '协议版本，如 v1.0、v2.0';
COMMENT
ON COLUMN "public"."agent_protocol"."content" IS '协议内容，JSON格式，包含消息结构、消息类型、命令列表等完整协议定义';
COMMENT
ON COLUMN "public"."agent_protocol"."create_user_id" IS '创建人用户ID';
COMMENT
ON COLUMN "public"."agent_protocol"."create_user_name" IS '创建人用户名称';
COMMENT
ON COLUMN "public"."agent_protocol"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."agent_protocol"."update_user_id" IS '修改人用户ID';
COMMENT
ON COLUMN "public"."agent_protocol"."update_user_name" IS '修改人用户名称';
COMMENT
ON COLUMN "public"."agent_protocol"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."agent_protocol"."reserve" IS '扩展字段，JSON格式';
COMMENT
ON COLUMN "public"."agent_protocol"."status" IS '状态: 1(启用) / 0(停用)';
COMMENT
ON TABLE "public"."agent_protocol" IS '执行器协议';

-- ----------------------------
-- Records of agent_protocol
-- ----------------------------

-- ----------------------------
-- Table structure for agent_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_rule";
CREATE TABLE "public"."agent_rule" (
                                       "id"       varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                       "agent_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_action" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
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
COMMENT
ON COLUMN "public"."agent_rule"."reserve" IS '扩展字段，JSON格式';
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
                                        "id"       varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                        "agent_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "definition_desc" text COLLATE "pg_catalog"."default" NOT NULL,
  "exec_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_data_format" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                        "user_id"  varchar(32) COLLATE "pg_catalog"."default",
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
COMMENT
ON COLUMN "public"."agent_skill"."reserve" IS '扩展字段，JSON格式';
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
INSERT INTO "public"."agent_skill"
VALUES ('2080291159512735744', '2079806936913846272',
        '通过模拟键盘快捷键操作，在Windows系统中打开指定的应用程序。执行流程：按下Win键打开开始菜单 → 输入应用程序名称进行搜索 → 等待搜索结果出现 → 按下回车键启动应用',
        '1. 模拟按下键盘Win键；2. 输入目标应用程序名称；3. 等待1-2秒让搜索结果加载；4. 模拟按下回车键确认打开',
        '{"status": "success|failed", "message": "操作结果描述", "appName": "应用程序名称"}', '2026-07-23 21:57:17.025', '2026-07-23 21:57:17.025', 1, NULL,
        '用于打开Windows应用程序的通用技能', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_model";
CREATE TABLE "public"."ai_model" (
                                     "id"          varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                     "provider_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
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
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT
ON COLUMN "public"."ai_model"."id" IS '主键';
COMMENT
ON COLUMN "public"."ai_model"."provider_id" IS 'AI模型供应商主键，关联 ai_model_provider.id';
COMMENT
ON COLUMN "public"."ai_model"."model_code" IS '模型编码，同一供应商下唯一';
COMMENT
ON COLUMN "public"."ai_model"."model_name" IS '模型名称，用户可读';
COMMENT ON COLUMN "public"."ai_model"."capability_config" IS '可扩展能力JSON文本，例如chat、vision、functionCalling';
COMMENT
ON COLUMN "public"."ai_model"."context_window" IS '上下文窗口大小（token数）';
COMMENT ON COLUMN "public"."ai_model"."provider_default" IS '供应商默认模型';
COMMENT ON COLUMN "public"."ai_model"."system_default" IS '系统默认模型，全局仅允许一个启用模型';
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
COMMENT
ON COLUMN "public"."ai_model"."reserve" IS '扩展字段，JSON格式';
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
                                              "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
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
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT
ON COLUMN "public"."ai_model_provider"."id" IS '主键';
COMMENT
ON COLUMN "public"."ai_model_provider"."provider_code" IS '供应商编码，全局唯一';
COMMENT
ON COLUMN "public"."ai_model_provider"."provider_name" IS '供应商名称，用户可读';
COMMENT ON COLUMN "public"."ai_model_provider"."protocol_type" IS '协议类型，首期仅支持OPENAI_COMPATIBLE';
COMMENT
ON COLUMN "public"."ai_model_provider"."base_url" IS 'API 基础地址';
COMMENT ON COLUMN "public"."ai_model_provider"."api_key_ciphertext" IS 'API Key AES-GCM加密密文，禁止回显、日志与审计复制';
COMMENT
ON COLUMN "public"."ai_model_provider"."timeout_millis" IS '请求超时时间（毫秒），默认60000';
COMMENT ON COLUMN "public"."ai_model_provider"."system_default" IS '是否系统默认供应商，仅辅助运维展示；实际默认由模型表确定';
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
COMMENT
ON COLUMN "public"."ai_model_provider"."reserve" IS '扩展字段，JSON格式';
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
    "id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "nickname"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "avatar_url"  varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
    "daily_quota" int4                                        NOT NULL DEFAULT 100,
    "used_quota"  int4                                        NOT NULL DEFAULT 0,
    "preferences" text COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"      int2                                        NOT NULL DEFAULT 1,
    "reserve"     text COLLATE "pg_catalog"."default",
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
ON COLUMN "public"."ai_user"."create_time" IS '创建时间';
COMMENT
ON COLUMN "public"."ai_user"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."ai_user"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."ai_user"."reserve" IS '扩展字段，JSON格式';
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
                                           "id"          varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "command" text COLLATE "pg_catalog"."default" NOT NULL,
  "role" text COLLATE "pg_catalog"."default" NOT NULL,
                                           "skill_id"    varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                           "user_id"     varchar(32) COLLATE "pg_catalog"."default",
                                           "executor_id" varchar(32) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."atomic_command"."id" IS '主键';
COMMENT ON COLUMN "public"."atomic_command"."name" IS '名称';
COMMENT ON COLUMN "public"."atomic_command"."command" IS '命令';
COMMENT ON COLUMN "public"."atomic_command"."role" IS '作用';
COMMENT ON COLUMN "public"."atomic_command"."skill_id" IS '智能体技能ID';
COMMENT ON COLUMN "public"."atomic_command"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."atomic_command"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."atomic_command"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."atomic_command"."reserve" IS '扩展字段，JSON格式';
COMMENT ON COLUMN "public"."atomic_command"."remark" IS '备注';
COMMENT
ON COLUMN "public"."atomic_command"."user_id" IS '用户归属ID，确保每个用户的原子命令私域隔离';
COMMENT
ON COLUMN "public"."atomic_command"."executor_id" IS '执行器类型外键，关联 agent_executor.id，替代旧的 executor_type 字符串字段';
COMMENT ON TABLE "public"."atomic_command" IS '原子命令';

-- ----------------------------
-- Records of atomic_command
-- ----------------------------
INSERT INTO "public"."atomic_command"
VALUES ('2080240470430375936', '打开微信', 'start weixin', '启动微信应用程序', '', '2026-07-23 18:35:51.806', '2026-07-23 18:35:51.806', 1, NULL,
        '打开Windows上的微信客户端', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080240492735684608', '微信搜索联系人', 'weixin_search_contact:文件传输助手', '在微信中搜索指定联系人', '', '2026-07-23 18:35:57.124',
        '2026-07-23 18:35:57.124', 1, NULL, '搜索并定位到文件传输助手', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080240505293430784', '发送微信消息', 'weixin_send_message:你好啊', '向当前微信聊天窗口发送消息', '', '2026-07-23 18:36:00.118',
        '2026-07-23 18:36:00.118', 1, NULL, '发送测试消息：你好啊', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080291176063459328', '打开微信', 'Win键 → 输入"微信" → 等待1000ms → 回车键', '在Windows系统中启动微信应用程序', '2080291159512735744',
        '2026-07-23 21:57:20.971', '2026-07-23 21:57:20.971', 1, NULL, '通过开始菜单搜索打开微信', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080503788197904384', '按下Win键', 'key_press:win', '模拟按下键盘Windows键，打开开始菜单', '', '2026-07-24 12:02:11.654', '2026-07-24 12:02:11.654', 1,
        NULL, '用于打开Windows开始菜单', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080503799749017600', '输入文本', 'type_text:{text}', '模拟键盘输入指定文本内容', '', '2026-07-24 12:02:14.408', '2026-07-24 12:02:14.408', 1, NULL,
        '用于在搜索框或输入框中输入文本', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080503809236533248', '等待', 'wait:{duration}', '等待指定的时间（秒）', '', '2026-07-24 12:02:16.67', '2026-07-24 12:02:16.67', 1, NULL,
        '用于操作之间的延迟等待', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080503818141040640', '按下回车键', 'key_press:enter', '模拟按下键盘回车键，确认当前选择', '', '2026-07-24 12:02:18.793', '2026-07-24 12:02:18.793', 1,
        NULL, '用于确认选择或启动应用', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080625504681263104', '按下Win键', 'keyboard.press.win', '模拟按下键盘Windows徽标键，打开开始菜单', '', '2026-07-24 20:05:51.125',
        '2026-07-24 20:05:51.125', 1, NULL, '用于打开开始菜单，配合应用搜索使用', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080625515141857280', '输入文本', 'keyboard.type.text', '模拟键盘输入指定的文本内容', '', '2026-07-24 20:05:53.619', '2026-07-24 20:05:53.619', 1,
        NULL, '用于在搜索框或输入框中输入文本', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080625529503154176', '等待', 'system.wait', '暂停执行指定时间，等待系统响应', '', '2026-07-24 20:05:57.043', '2026-07-24 20:05:57.043', 1, NULL,
        '用于等待搜索结果加载或界面渲染完成', NULL, NULL);
INSERT INTO "public"."atomic_command"
VALUES ('2080625529511542784', '按下回车键', 'keyboard.press.enter', '模拟按下键盘回车键，确认选择或执行', '', '2026-07-24 20:05:57.045',
        '2026-07-24 20:05:57.045', 1, NULL, '用于确认打开选中的应用或执行命令', NULL, NULL);

-- ----------------------------
-- Table structure for chat_turn
-- ----------------------------
DROP TABLE IF EXISTS "public"."chat_turn";
CREATE TABLE "public"."chat_turn"
(
    "id"                   varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "session_id"           varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "turn_number"          int4                                        NOT NULL,
    "user_message_id"      varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "assistant_message_id" varchar(32) COLLATE "pg_catalog"."default",
    "task_id"              varchar(32) COLLATE "pg_catalog"."default",
    "reasoning_summary"    jsonb,
    "create_time"          timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time"          timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"               int2                                        NOT NULL DEFAULT 1,
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
-- Records of chat_turn
-- ----------------------------
INSERT INTO "public"."chat_turn"
VALUES ('2080137398186201088', '2080137334441168896', 1, '2080137398140063744', NULL, '', NULL, '2026-07-23 11:46:17.468', '2026-07-23 11:46:17.468', 1, NULL,
        '');
INSERT INTO "public"."chat_turn"
VALUES ('2080240362850672640', '2080240285419626496', 1, '2080240362812923904', NULL, '', NULL, '2026-07-23 18:35:26.157', '2026-07-23 18:35:26.157', 1, NULL,
        '');
INSERT INTO "public"."chat_turn"
VALUES ('2080267856010047488', '2080267812255068160', 1, '2080267855976493056', '2080267870945964032', '', NULL, '2026-07-23 20:24:41.037',
        '2026-07-23 20:24:41.037', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080267971613454336', '2080267812255068160', 2, '2080267971596677120', '2080268032195981312', '', NULL, '2026-07-23 20:25:08.599',
        '2026-07-23 20:25:08.599', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080271939085291520', '2080271927366406144', 1, '2080271939051737088', '2080271982936739840', '', NULL, '2026-07-23 20:40:54.518',
        '2026-07-23 20:40:54.518', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080273956658802688', '2080273869140455424', 1, '2080273956621053952', '2080274109528600576', '', NULL, '2026-07-23 20:48:55.545',
        '2026-07-23 20:48:55.545', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080275201146499072', '2080273869140455424', 2, '2080275201100361728', '2080275239138504704', '', NULL, '2026-07-23 20:53:52.254',
        '2026-07-23 20:53:52.254', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080280355811921920', '2080280339907121152', 1, '2080280355786756096', '2080280388489744384', '', NULL, '2026-07-23 21:14:21.222',
        '2026-07-23 21:14:21.222', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080283794981842944', '2080283764233400320', 1, '2080283794927316992', '2080283827911323648', '', NULL, '2026-07-23 21:28:01.184',
        '2026-07-23 21:28:01.184', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080285348874952704', '2080283764233400320', 2, '2080285348812038144', '2080285417376325632', '', NULL, '2026-07-23 21:34:11.661',
        '2026-07-23 21:34:11.661', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080291027190833152', '2080291017908838400', 1, '2080291027165667328', '2080291258989043712', '', NULL, '2026-07-23 21:56:45.477',
        '2026-07-23 21:56:45.477', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080503671730470912', '2080291017908838400', 2, '2080503671659167744', '2080503863418552320', '', NULL, '2026-07-24 12:01:43.886',
        '2026-07-24 12:01:43.886', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080625379049275392', '2080625360162324480', 1, '2080625379019915264', '2080625587858505728', '', NULL, '2026-07-24 20:05:21.172',
        '2026-07-24 20:05:21.172', 1, NULL, '');
INSERT INTO "public"."chat_turn"
VALUES ('2080642571732037632', '2080642554497642496', 1, '2080642571690094592', '2080642642179567616', '', NULL, '2026-07-24 21:13:40.227',
        '2026-07-24 21:13:40.227', 1, NULL, '');

-- ----------------------------
-- Table structure for execution_event
-- ----------------------------
DROP TABLE IF EXISTS "public"."execution_event";
CREATE TABLE "public"."execution_event"
(
    "id"                  varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "turn_id"             varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "task_id"             varchar(32) COLLATE "pg_catalog"."default",
    "task_detail_id"      varchar(32) COLLATE "pg_catalog"."default",
    "event_type"          varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "step_name"           varchar(255) COLLATE "pg_catalog"."default",
    "command_name"        varchar(255) COLLATE "pg_catalog"."default",
    "command_content"     text COLLATE "pg_catalog"."default",
    "response_content"    text COLLATE "pg_catalog"."default",
    "failure_reason"      text COLLATE "pg_catalog"."default",
    "sequence_no"         int4                                       NOT NULL,
    "started_at"          timestamp(6),
    "finished_at"         timestamp(6),
    "atomic_command_id"   varchar(32) COLLATE "pg_catalog"."default",
    "atomic_command_code" varchar(128) COLLATE "pg_catalog"."default",
    "provider_id"         varchar(32) COLLATE "pg_catalog"."default",
    "provider_name"       varchar(128) COLLATE "pg_catalog"."default",
    "model_id"            varchar(32) COLLATE "pg_catalog"."default",
    "model_code"          varchar(128) COLLATE "pg_catalog"."default",
    "create_time"         timestamp(6)                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status"              int2                                       NOT NULL DEFAULT 1
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
-- Records of execution_event
-- ----------------------------
INSERT INTO "public"."execution_event"
VALUES ('2080137398223949824', '2080137398186201088', '2080137398211366912', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 11:46:17.477', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.477', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398299447296', '2080137398186201088', '2080137398211366912', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 11:46:17.495', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.495', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398307835904', '2080137398186201088', '2080137398211366912', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 11:46:17.497', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.497', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398324613120', '2080137398186201088', '2080137398211366912', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 11:46:17.501', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.501', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398333001728', '2080137398186201088', '2080137398211366912', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 11:46:17.503', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.503', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398345584640', '2080137398186201088', '2080137398211366912', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 11:46:17.505', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.506', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398358167552', '2080137398186201088', '2080137398211366912', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 11:46:17.509', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.509', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137398366556160', '2080137398186201088', '2080137398211366912', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 11:46:17.511', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:17.511', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080137430520090624', '2080137398186201088', '2080137398211366912', '', 'TASK_FAILED', '任务执行失败', '', '', '', '执行器类型[win_rpa]不存在', 9,
        '2026-07-23 11:46:25.177', '2026-07-23 11:46:25.177', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 11:46:25.177', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240362909392896', '2080240362850672640', '2080240362880032768', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 18:35:26.168', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.171', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240362997473280', '2080240362850672640', '2080240362880032768', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 18:35:26.192', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.192', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363005861888', '2080240362850672640', '2080240362880032768', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 18:35:26.194', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.194', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363022639104', '2080240362850672640', '2080240362880032768', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 18:35:26.198', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.198', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363031027712', '2080240362850672640', '2080240362880032768', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 18:35:26.199', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.2', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363039416320', '2080240362850672640', '2080240362880032768', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 18:35:26.201', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.202', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363051999232', '2080240362850672640', '2080240362880032768', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 18:35:26.205', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.205', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240363060387840', '2080240362850672640', '2080240362880032768', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 18:35:26.207', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 18:35:26.207', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240536390000640', '2080240362850672640', '2080240362880032768', '', 'AI_COMPLETED', 'AI 探索方案生成完成', '', '', '', '', 9,
        '2026-07-23 18:36:07.532', NULL, NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 18:36:07.532', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080240536452915200', '2080240362850672640', '2080240362880032768', '', 'TASK_COMPLETED', '任务执行成功', '', '', '', '', 10,
        '2026-07-23 18:36:07.547', '2026-07-23 18:36:07.547', NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 18:36:07.547', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856051990528', '2080267856010047488', '2080267856031019008', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 20:24:41.046', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.047', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856135876608', '2080267856010047488', '2080267856031019008', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 20:24:41.066', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.067', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856144265216', '2080267856010047488', '2080267856031019008', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 20:24:41.068', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.069', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856156848128', '2080267856010047488', '2080267856031019008', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 20:24:41.072', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.072', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856165236736', '2080267856010047488', '2080267856031019008', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 20:24:41.074', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.074', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856177819648', '2080267856010047488', '2080267856031019008', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 20:24:41.076', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.077', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856190402560', '2080267856010047488', '2080267856031019008', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 20:24:41.08', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.08', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267856202985472', '2080267856010047488', '2080267856031019008', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 20:24:41.082', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:24:41.083', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267870899826688', '2080267856010047488', '2080267856031019008', '', 'AI_COMPLETED', 'AI 探索方案生成完成', '', '', '', '', 9,
        '2026-07-23 20:24:44.586', NULL, NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 20:24:44.587', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267870929186816', '2080267856010047488', '2080267856031019008', '', 'TASK_COMPLETED', '任务执行成功', '', '', '', '', 10,
        '2026-07-23 20:24:44.594', '2026-07-23 20:24:44.594', NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 20:24:44.594', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971626037248', '2080267971613454336', '2080267971621842944', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 20:25:08.602', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.602', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971667980288', '2080267971613454336', '2080267971621842944', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 20:25:08.611', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.612', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971672174592', '2080267971613454336', '2080267971621842944', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 20:25:08.613', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.613', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971680563200', '2080267971613454336', '2080267971621842944', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 20:25:08.615', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.615', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971693146112', '2080267971613454336', '2080267971621842944', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 20:25:08.618', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.618', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971701534720', '2080267971613454336', '2080267971621842944', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 20:25:08.62', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.62', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971714117632', '2080267971613454336', '2080267971621842944', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 20:25:08.623', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.623', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080267971722506240', '2080267971613454336', '2080267971621842944', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 20:25:08.625', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:08.625', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080268032183398400', '2080267971613454336', '2080267971621842944', '', 'TASK_FAILED', '任务执行失败', '', '', '', '当前登录用户身份为空', 9,
        '2026-07-23 20:25:23.04', '2026-07-23 20:25:23.04', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:25:23.04', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939123040256', '2080271939085291520', '2080271939106263040', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 20:40:54.527', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.527', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939206926336', '2080271939085291520', '2080271939106263040', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 20:40:54.546', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.547', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939215314944', '2080271939085291520', '2080271939106263040', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 20:40:54.548', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.549', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939223703552', '2080271939085291520', '2080271939106263040', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 20:40:54.55', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.551', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939236286464', '2080271939085291520', '2080271939106263040', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 20:40:54.554', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.554', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939248869376', '2080271939085291520', '2080271939106263040', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 20:40:54.556', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.557', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939261452288', '2080271939085291520', '2080271939106263040', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 20:40:54.56', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.56', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271939274035200', '2080271939085291520', '2080271939106263040', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 20:40:54.562', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:40:54.563', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080271982919962624', '2080271939085291520', '2080271939106263040', '', 'TASK_FAILED', '任务执行失败', '', '', '', '客户端实例[win_rpa]不存在', 9,
        '2026-07-23 20:41:04.969', '2026-07-23 20:41:04.969', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:41:04.969', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956700745728', '2080273956658802688', '2080273956683968512', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 20:48:55.554', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.555', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956784631808', '2080273956658802688', '2080273956683968512', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 20:48:55.575', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.575', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956793020416', '2080273956658802688', '2080273956683968512', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 20:48:55.577', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.577', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956809797632', '2080273956658802688', '2080273956683968512', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 20:48:55.581', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.581', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956822380544', '2080273956658802688', '2080273956683968512', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 20:48:55.583', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.584', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956834963456', '2080273956658802688', '2080273956683968512', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 20:48:55.586', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.587', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956851740672', '2080273956658802688', '2080273956683968512', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 20:48:55.591', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.591', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080273956864323584', '2080273956658802688', '2080273956683968512', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 20:48:55.593', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:48:55.594', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080274109516017664', '2080273956658802688', '2080273956683968512', '', 'TASK_FAILED', '任务执行失败', '', '', '', '智能体[2079542278239834112]不存在',
        9, '2026-07-23 20:49:31.988', '2026-07-23 20:49:31.988', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:49:31.989', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201230385152', '2080275201146499072', '2080275201213607936', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 20:53:52.273', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.274', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201335242752', '2080275201146499072', '2080275201213607936', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 20:53:52.299', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.299', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201343631360', '2080275201146499072', '2080275201213607936', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 20:53:52.301', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.301', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201352019968', '2080275201146499072', '2080275201213607936', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 20:53:52.302', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.303', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201356214272', '2080275201146499072', '2080275201213607936', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 20:53:52.304', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.304', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201368797184', '2080275201146499072', '2080275201213607936', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 20:53:52.306', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.307', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201381380096', '2080275201146499072', '2080275201213607936', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 20:53:52.31', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.31', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275201414934528', '2080275201146499072', '2080275201213607936', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 20:53:52.314', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:53:52.318', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080275239121727488', '2080275201146499072', '2080275201213607936', '', 'TASK_FAILED', '任务执行失败', '', '', '', '智能体[当前智能体]不存在', 9,
        '2026-07-23 20:54:01.308', '2026-07-23 20:54:01.308', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 20:54:01.308', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355853864960', '2080280355811921920', '2080280355837087744', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 21:14:21.231', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.232', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355941945344', '2080280355811921920', '2080280355837087744', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 21:14:21.253', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.253', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355954528256', '2080280355811921920', '2080280355837087744', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 21:14:21.255', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.256', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355962916864', '2080280355811921920', '2080280355837087744', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 21:14:21.257', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.258', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355971305472', '2080280355811921920', '2080280355837087744', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 21:14:21.259', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.26', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280355983888384', '2080280355811921920', '2080280355837087744', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 21:14:21.262', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.263', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280356004859904', '2080280355811921920', '2080280355837087744', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 21:14:21.268', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.268', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280356017442816', '2080280355811921920', '2080280355837087744', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 21:14:21.27', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:21.271', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080280388477161472', '2080280355811921920', '2080280355837087744', '', 'TASK_FAILED', '任务执行失败', '', '', '', '当前登录用户身份为空', 9,
        '2026-07-23 21:14:29.01', '2026-07-23 21:14:29.01', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:14:29.01', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795040563200', '2080283794981842944', '2080283795015397376', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 21:28:01.196', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.198', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795158003712', '2080283794981842944', '2080283795015397376', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 21:28:01.226', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.226', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795170586624', '2080283794981842944', '2080283795015397376', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 21:28:01.229', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.229', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795199946752', '2080283794981842944', '2080283795015397376', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 21:28:01.236', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.236', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795212529664', '2080283794981842944', '2080283795015397376', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 21:28:01.239', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.239', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795233501184', '2080283794981842944', '2080283795015397376', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 21:28:01.242', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.244', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795254472704', '2080283794981842944', '2080283795015397376', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 21:28:01.249', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.249', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283795271249920', '2080283794981842944', '2080283795015397376', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 21:28:01.252', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:01.253', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080283827890352128', '2080283794981842944', '2080283795015397376', '', 'TASK_FAILED', '任务执行失败', '', '', '', '当前登录用户身份为空', 9,
        '2026-07-23 21:28:09.03', '2026-07-23 21:28:09.03', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:28:09.03', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285348979810304', '2080285348874952704', '2080285348954644480', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 21:34:11.685', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.686', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349109833728', '2080285348874952704', '2080285348954644480', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 21:34:11.717', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.717', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349122416640', '2080285348874952704', '2080285348954644480', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3, '2026-07-23 21:34:11.72',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.72', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349139193856', '2080285348874952704', '2080285348954644480', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 21:34:11.724', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.724', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349155971072', '2080285348874952704', '2080285348954644480', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 21:34:11.728', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.728', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349197914112', '2080285348874952704', '2080285348954644480', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 21:34:11.731', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.738', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349235662848', '2080285348874952704', '2080285348954644480', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 21:34:11.747', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.747', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285349256634368', '2080285348874952704', '2080285348954644480', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 21:34:11.751', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:11.752', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080285417351159808', '2080285348874952704', '2080285348954644480', '', 'TASK_FAILED', '任务执行失败', '', '', '', '不支持的过期时间单位[{}]', 9,
        '2026-07-23 21:34:27.987', '2026-07-23 21:34:27.987', NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:34:27.987', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027245359104', '2080291027190833152', '2080291027220193280', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-23 21:56:45.489', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.49', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027366993920', '2080291027190833152', '2080291027220193280', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-23 21:56:45.518', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.519', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027379576832', '2080291027190833152', '2080291027220193280', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-23 21:56:45.522', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.522', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027404742656', '2080291027190833152', '2080291027220193280', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-23 21:56:45.527', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.528', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027417325568', '2080291027190833152', '2080291027220193280', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-23 21:56:45.531', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.531', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027442491392', '2080291027190833152', '2080291027220193280', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-23 21:56:45.535', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.537', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027467657216', '2080291027190833152', '2080291027220193280', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-23 21:56:45.543', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.543', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291027484434432', '2080291027190833152', '2080291027220193280', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-23 21:56:45.546', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-23 21:56:45.547', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291258892574720', '2080291027190833152', '2080291027220193280', '', 'AI_COMPLETED', 'AI 探索方案生成完成', '', '', '', '', 9,
        '2026-07-23 21:57:40.719', NULL, NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 21:57:40.719', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080291258955489280', '2080291027190833152', '2080291027220193280', '', 'TASK_COMPLETED', '任务执行成功', '', '', '', '', 10,
        '2026-07-23 21:57:40.734', '2026-07-23 21:57:40.734', NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-23 21:57:40.734', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671772413952', '2080503671730470912', '2080503671755636736', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-24 12:01:43.895', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.896', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671864688640', '2080503671730470912', '2080503671755636736', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-24 12:01:43.918', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.918', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671873077248', '2080503671730470912', '2080503671755636736', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3, '2026-07-24 12:01:43.92',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.92', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671881465856', '2080503671730470912', '2080503671755636736', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-24 12:01:43.922', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.922', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671894048768', '2080503671730470912', '2080503671755636736', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-24 12:01:43.925', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.925', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671915020288', '2080503671730470912', '2080503671755636736', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-24 12:01:43.929', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.93', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671931797504', '2080503671730470912', '2080503671755636736', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-24 12:01:43.933', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.934', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503671940186112', '2080503671730470912', '2080503671755636736', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-24 12:01:43.936', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 12:01:43.936', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503863380803584', '2080503671730470912', '2080503671755636736', '', 'AI_COMPLETED', 'AI 探索方案生成完成', '', '', '', '', 9,
        '2026-07-24 12:02:29.578', NULL, NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-24 12:02:29.579', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080503863401775104', '2080503671730470912', '2080503671755636736', '', 'TASK_COMPLETED', '任务执行成功', '', '', '', '', 10,
        '2026-07-24 12:02:29.584', '2026-07-24 12:02:29.584', NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-24 12:02:29.584', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379087024128', '2080625379049275392', '2080625379074441216', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-24 20:05:21.181', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.181', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379179298816', '2080625379049275392', '2080625379074441216', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-24 20:05:21.203', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.203', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379187687424', '2080625379049275392', '2080625379074441216', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-24 20:05:21.205', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.205', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379204464640', '2080625379049275392', '2080625379074441216', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-24 20:05:21.209', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.209', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379212853248', '2080625379049275392', '2080625379074441216', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-24 20:05:21.21', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.211', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379225436160', '2080625379049275392', '2080625379074441216', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-24 20:05:21.213', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.214', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379242213376', '2080625379049275392', '2080625379074441216', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-24 20:05:21.217', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.218', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625379250601984', '2080625379049275392', '2080625379074441216', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-24 20:05:21.219', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 20:05:21.22', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625587812368384', '2080625379049275392', '2080625379074441216', '', 'AI_COMPLETED', 'AI 探索方案生成完成', '', '', '', '', 9,
        '2026-07-24 20:06:10.944', NULL, NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-24 20:06:10.945', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080625587837534208', '2080625379049275392', '2080625379074441216', '', 'TASK_COMPLETED', '任务执行成功', '', '', '', '', 10,
        '2026-07-24 20:06:10.951', '2026-07-24 20:06:10.951', NULL, NULL, NULL, '词元之河', NULL, 'deepseek-v4-pro', '2026-07-24 20:06:10.951', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571782369280', '2080642571732037632', '2080642571761397760', '', 'CONTEXT_ASSEMBLING', '正在装配智能体上下文', '', '', '', '', 1,
        '2026-07-24 21:13:40.238', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.239', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571899809792', '2080642571732037632', '2080642571761397760', '', 'CONTEXT_ASSEMBLED', '智能体定义已装配', '', '', '', '', 2,
        '2026-07-24 21:13:40.267', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.267', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571912392704', '2080642571732037632', '2080642571761397760', '', 'RULE_LOADED', '智能体规则已装配', '', '', '', '', 3,
        '2026-07-24 21:13:40.269', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.27', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571929169920', '2080642571732037632', '2080642571761397760', '', 'SKILL_LOADED', '智能体技能已装配', '', '', '', '', 4,
        '2026-07-24 21:13:40.273', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.274', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571937558528', '2080642571732037632', '2080642571761397760', '', 'SUB_AGENT_LOADED', '子智能体关系已装配', '', '', '', '', 5,
        '2026-07-24 21:13:40.276', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.276', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571954335744', '2080642571732037632', '2080642571761397760', '', 'MEMORY_MATCHING', '正在匹配候选记忆', '', '', '', '', 6,
        '2026-07-24 21:13:40.279', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.28', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571971112960', '2080642571732037632', '2080642571761397760', '', 'MEMORY_MISSED', '未命中候选记忆，转入 AI 探索', '', '', '', '', 7,
        '2026-07-24 21:13:40.284', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.284', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642571983695872', '2080642571732037632', '2080642571761397760', '', 'AI_STARTED', 'AI 开始生成探索方案', '', '', '', '', 8,
        '2026-07-24 21:13:40.285', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-24 21:13:40.287', 1);
INSERT INTO "public"."execution_event"
VALUES ('2080642642162790400', '2080642571732037632', '2080642571761397760', '', 'TASK_FAILED', '任务执行失败', '', '', '',
        '客户端实例[2079542278239834112]不存在', 9, '2026-07-24 21:13:57.019', '2026-07-24 21:13:57.019', NULL, NULL, NULL, NULL, NULL, NULL,
        '2026-07-24 21:13:57.019', 1);

-- ----------------------------
-- Table structure for sub_agent_relation
-- ----------------------------
DROP TABLE IF EXISTS "public"."sub_agent_relation";
CREATE TABLE "public"."sub_agent_relation" (
                                               "id"            varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                               "main_agent_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                               "sub_agent_id"  varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
  "reserve" text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."sub_agent_relation"."id" IS '主键';
COMMENT ON COLUMN "public"."sub_agent_relation"."main_agent_id" IS '主智能体';
COMMENT ON COLUMN "public"."sub_agent_relation"."sub_agent_id" IS '子智能体';
COMMENT ON COLUMN "public"."sub_agent_relation"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sub_agent_relation"."update_time" IS '修改时间';
COMMENT
ON COLUMN "public"."sub_agent_relation"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."sub_agent_relation"."reserve" IS '扩展字段，JSON格式';
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
                                 "id"                varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
                                 "agent_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                 "parent_task_id"    varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                 "next_task_id"      varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                 "step_type"         int2                                        NOT NULL DEFAULT 1,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
                                 "exec_status"       int2                                        NOT NULL DEFAULT 1,
  "failure_reason" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::text,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
                                 "reserve"           text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                 "provider_id"       varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                 "model_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                 "model_code"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                 "user_id"           varchar(32) COLLATE "pg_catalog"."default",
                                 "client_id"         varchar(32) COLLATE "pg_catalog"."default",
                                 "dispatch_id"       varchar(32) COLLATE "pg_catalog"."default",
                                 "memory_id"         varchar(32) COLLATE "pg_catalog"."default"           DEFAULT ''::character varying,
                                 "memory_version_no" int4
)
;
COMMENT ON COLUMN "public"."task"."id" IS '主键';
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
COMMENT
ON COLUMN "public"."task"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."task"."reserve" IS '扩展字段，JSON格式';
COMMENT ON COLUMN "public"."task"."remark" IS '备注';
COMMENT
ON COLUMN "public"."task"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."task"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."task"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."task"."model_code" IS '运行模型编码快照';
COMMENT
ON COLUMN "public"."task"."user_id" IS '用户归属ID，确保任务归属到具体用户';
COMMENT
ON COLUMN "public"."task"."client_id" IS '执行客户端主键，关联 agent_client.id，记录由哪个客户端执行';
COMMENT
ON COLUMN "public"."task"."dispatch_id" IS '下发批次标识，服务端雪花ID，关联一次 WebSocket 批量命令下发';
COMMENT
ON COLUMN "public"."task"."memory_id" IS '关联记忆主键，对应 agent_memory.id';
COMMENT
ON COLUMN "public"."task"."memory_version_no" IS '执行时的记忆版本号快照';
COMMENT ON TABLE "public"."task" IS '任务';

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO "public"."task"
VALUES ('2080642571761397760', '2079806936913846272', '人机对话', '', '', 2, '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"打开微信","clientId":"2080625787096334336","sessionId":"2080642554497642496","modelId":"2077431632937414656"}',
        '', 4, '客户端实例[2079542278239834112]不存在', '2026-07-24 21:13:40.234', '2026-07-24 21:13:40.234', 1, '', '智能体命令调度任务', '', '', '', '', NULL,
        NULL, NULL, '', NULL);

-- ----------------------------
-- Table structure for task_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."task_detail";
CREATE TABLE "public"."task_detail" (
                                        "id"                varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
                                        "task_id"           varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                        "parent_task_id"    varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                        "next_task_id"      varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
  "branch_condition" text COLLATE "pg_catalog"."default" NOT NULL,
  "branch_route" text COLLATE "pg_catalog"."default" NOT NULL,
  "request_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "return_params" text COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "status" int2 NOT NULL DEFAULT 1,
                                        "reserve"           text COLLATE "pg_catalog"."default",
  "remark" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                        "provider_id"       varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
  "provider_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                        "model_id"          varchar(32) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT ''::character varying,
                                        "model_code"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
                                        "command_id"        varchar(32) COLLATE "pg_catalog"."default",
                                        "atomic_command_id" varchar(32) COLLATE "pg_catalog"."default",
                                        "client_id"         varchar(32) COLLATE "pg_catalog"."default",
  "sequence_no" int4,
                                        "dispatch_id"       varchar(32) COLLATE "pg_catalog"."default",
                                        "step_type"         int2                                        NOT NULL DEFAULT 1,
                                        "exec_status"       int2                                        NOT NULL DEFAULT 1
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
COMMENT
ON COLUMN "public"."task_detail"."status" IS '状态: ON(启用) / OFF(停用)';
COMMENT
ON COLUMN "public"."task_detail"."reserve" IS '扩展字段，JSON格式';
COMMENT ON COLUMN "public"."task_detail"."remark" IS '备注';
COMMENT
ON COLUMN "public"."task_detail"."provider_id" IS '运行供应商主键快照';
COMMENT
ON COLUMN "public"."task_detail"."provider_name" IS '运行供应商名称快照';
COMMENT
ON COLUMN "public"."task_detail"."model_id" IS '运行模型主键快照';
COMMENT
ON COLUMN "public"."task_detail"."model_code" IS '运行模型编码快照';
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
VALUES ('2080642642141818880', '2080642571761397760', '人机对话', '', '', '', '',
        '{"agentId":"2079806936913846272","commandName":"人机对话","commandContent":"打开微信","clientId":"2080625787096334336","sessionId":"2080642554497642496","modelId":"2077431632937414656"}',
        '客户端实例[2079542278239834112]不存在', '2026-07-24 21:13:57.014', '2026-07-24 21:13:57.014', 1, '', '智能体命令调度失败详情', '', '', '', '', NULL,
        NULL, NULL, NULL, NULL, 1, 4);

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
CREATE INDEX "idx_agent_chat_message_turn" ON "public"."agent_chat_message" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
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
CREATE INDEX "idx_agent_chat_session_create_user" ON "public"."agent_chat_session" USING btree (
    "create_user_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
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
-- Indexes structure for table agent_executor
-- ----------------------------
CREATE INDEX "idx_agent_executor_protocol_id" ON "public"."agent_executor" USING btree (
    "protocol_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

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
CREATE INDEX "idx_agent_memory_parent_memory_id" ON "public"."agent_memory" USING btree (
    "parent_memory_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_agent_memory_user_id" ON "public"."agent_memory" USING btree (
    "user_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_agent_memory_version_status" ON "public"."agent_memory" USING btree (
    "agent_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "version_status" "pg_catalog"."int2_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table agent_memory
-- ----------------------------
ALTER TABLE "public"."agent_memory" ADD CONSTRAINT "agent_memory_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table agent_memory_step
-- ----------------------------
CREATE INDEX "idx_agent_memory_step_memory_seq" ON "public"."agent_memory_step" USING btree (
    "memory_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "sequence_no" "pg_catalog"."int4_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table agent_memory_step
-- ----------------------------
ALTER TABLE "public"."agent_memory_step"
    ADD CONSTRAINT "agent_memory_step_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table agent_protocol
-- ----------------------------
ALTER TABLE "public"."agent_protocol"
    ADD CONSTRAINT "agent_protocol_protocol_code_key" UNIQUE ("protocol_code");

-- ----------------------------
-- Primary Key structure for table agent_protocol
-- ----------------------------
ALTER TABLE "public"."agent_protocol"
    ADD CONSTRAINT "agent_protocol_pkey" PRIMARY KEY ("id");

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
-- Indexes structure for table chat_turn
-- ----------------------------
CREATE INDEX "idx_chat_turn_session" ON "public"."chat_turn" USING btree (
    "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_chat_turn_session_turn" ON "public"."chat_turn" USING btree (
    "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "turn_number" "pg_catalog"."int4_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_chat_turn_task" ON "public"."chat_turn" USING btree (
    "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table chat_turn
-- ----------------------------
ALTER TABLE "public"."chat_turn"
    ADD CONSTRAINT "chat_turn_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table execution_event
-- ----------------------------
CREATE INDEX "idx_execution_event_task" ON "public"."execution_event" USING btree (
    "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_turn" ON "public"."execution_event" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_turn_seq" ON "public"."execution_event" USING btree (
    "turn_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "sequence_no" "pg_catalog"."int4_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_execution_event_type" ON "public"."execution_event" USING btree (
    "event_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table execution_event
-- ----------------------------
ALTER TABLE "public"."execution_event"
    ADD CONSTRAINT "execution_event_pkey" PRIMARY KEY ("id");

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
CREATE INDEX "idx_task_memory_id" ON "public"."task" USING btree (
    "memory_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
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