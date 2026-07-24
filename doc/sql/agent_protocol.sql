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
ON COLUMN "public"."agent_protocol"."description" IS '协议描述，说明该协议的主要特点和适用范围';
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
ON COLUMN "public"."agent_protocol"."remark" IS '备注';
COMMENT
ON COLUMN "public"."agent_protocol"."status" IS '状态: 1(启用) / 0(停用)';
COMMENT
ON TABLE "public"."agent_protocol" IS '执行器协议';

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
-- 删除 description 和 remark 字段（2026-07-24）
-- ----------------------------
ALTER TABLE "public"."agent_protocol" DROP COLUMN IF EXISTS "description";
ALTER TABLE "public"."agent_protocol" DROP COLUMN IF EXISTS "remark";
ALTER TABLE "public"."agent_executor"
    ADD COLUMN "protocol_id" varchar(32) COLLATE "pg_catalog"."default";

COMMENT
ON COLUMN "public"."agent_executor"."protocol_id" IS '协议外键，关联 agent_protocol.id，标识该执行器使用的对接协议';

-- ----------------------------
-- 索引结构 for table agent_executor (protocol_id)
-- ----------------------------
CREATE INDEX "idx_agent_executor_protocol_id" ON "public"."agent_executor" ("protocol_id");