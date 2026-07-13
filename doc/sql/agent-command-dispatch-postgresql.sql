-- ============================================================
-- simple-ai 智能体命令调度运行辅助 PostgreSQL 脚本
-- 说明：补充调度链路常用索引、安全占位原子命令样例，
--        以及 atomic_command 新增 skill_id 字段的结构变更。
--
-- 部署前置条件：本脚本仅用于已有且隔离的 PostgreSQL 数据库；执行前必须
-- 完成可验证备份并确认恢复路径。禁止将本脚本与全量建表脚本混用。
--
-- 执行顺序：先审阅 atomic_command.skill_id 与 task.agent_id 的现有结构和
-- 数据量，再执行 DDL；随后核对 UPDATE task ... FROM agent_memory 的预期
-- 回填行数，最后执行索引、占位数据和聊天表创建。所有 CREATE/ADD 使用
-- IF NOT EXISTS，固定 ID 占位数据用 WHERE NOT EXISTS，因此重复执行不会
-- 重复建对象或插入占位行；但 UPDATE 仍应在维护窗口审阅并记录影响行数。
--
-- 回滚前置：DDL 与数据变更的回滚依赖执行前备份。若需回退，应由 DBA 按
-- 已记录对象清单和备份恢复，禁止在未核对依赖与数据保留要求时直接 DROP。
-- 本仓库不会自动执行本脚本。
-- ============================================================

-- ============================================================
-- 原子命令表结构变更：新增 skill_id 字段
-- ============================================================
ALTER TABLE atomic_command
    ADD COLUMN IF NOT EXISTS skill_id VARCHAR(255) NOT NULL DEFAULT '';

COMMENT ON COLUMN atomic_command.skill_id IS '智能体技能ID';

-- ============================================================
-- 任务直接智能体归属结构变更
-- ============================================================
ALTER TABLE task
    ADD COLUMN IF NOT EXISTS agent_id VARCHAR(255) NOT NULL DEFAULT '';

COMMENT ON COLUMN task.agent_id IS '智能体主键';

UPDATE task t
SET agent_id = am.agent_id
FROM agent_memory am
WHERE am.id = t.agent_memory_id
  AND t.agent_id = '';

-- ============================================================
-- 智能体直属配置查询索引
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_agent_rule_agent_status
    ON agent_rule (agent_id, status);

CREATE INDEX IF NOT EXISTS idx_agent_skill_agent_status
    ON agent_skill (agent_id, status);

CREATE INDEX IF NOT EXISTS idx_sub_agent_relation_main_status
    ON sub_agent_relation (main_agent_id, status);

-- ============================================================
-- 记忆复用与步骤链查询索引
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_agent_memory_agent_status
    ON agent_memory (agent_id, status);

CREATE INDEX IF NOT EXISTS idx_agent_memory_detail_memory_status
    ON agent_memory_detail (agent_memory_id, status);

CREATE INDEX IF NOT EXISTS idx_agent_memory_detail_next_step
    ON agent_memory_detail (next_step_id);

CREATE INDEX IF NOT EXISTS idx_agent_memory_detail_parent_step
    ON agent_memory_detail (parent_step_id);

-- ============================================================
-- 原子命令匹配与任务追踪索引
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_atomic_command_skill_status
    ON atomic_command (skill_id, status);

CREATE INDEX IF NOT EXISTS idx_atomic_command_status
    ON atomic_command (status);

CREATE INDEX IF NOT EXISTS idx_atomic_command_name_status
    ON atomic_command (name, status);

CREATE INDEX IF NOT EXISTS idx_task_parent_status
    ON task (parent_task_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_agent_update
    ON task (agent_id, update_time DESC);

CREATE INDEX IF NOT EXISTS idx_task_memory_status
    ON task (agent_memory_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_detail_task_status
    ON task_detail (task_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_detail_parent_next
    ON task_detail (parent_task_id, next_task_id);

-- ============================================================
-- 安全占位原子命令样例（skill_id 置空，表示全局通用命令）
-- ============================================================
INSERT INTO atomic_command (
    id,
    name,
    command,
    role,
    skill_id,
    create_time,
    update_time,
    status,
    reserver,
    remark
)
SELECT
    'atomic-readonly-info-default',
    '只读信息查询',
    'READ_ONLY_INFO',
    'READ',
    '',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    '',
    '安全占位命令，仅用于只读信息类执行器识别'
WHERE NOT EXISTS (
    SELECT 1
    FROM atomic_command
    WHERE id = 'atomic-readonly-info-default'
);

INSERT INTO atomic_command (
    id,
    name,
    command,
    role,
    skill_id,
    create_time,
    update_time,
    status,
    reserver,
    remark
)
SELECT
    'atomic-sub-agent-default',
    '子智能体调度',
    'SUB_AGENT_DISPATCH',
    'SUB_AGENT',
    '',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    '',
    '安全占位命令，仅用于子智能体调度识别，真实递归由调度门面执行'
WHERE NOT EXISTS (
    SELECT 1
    FROM atomic_command
    WHERE id = 'atomic-sub-agent-default'
);

INSERT INTO atomic_command (
    id,
    name,
    command,
    role,
    skill_id,
    create_time,
    update_time,
    status,
    reserver,
    remark
)
SELECT
    'atomic-write-safe-placeholder',
    '写入类安全占位',
    'WRITE_SAFE_PLACEHOLDER',
    'WRITE',
    '',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    '',
    '安全占位命令，不执行真实写入，等待白名单规则接入'
WHERE NOT EXISTS (
    SELECT 1
    FROM atomic_command
    WHERE id = 'atomic-write-safe-placeholder'
);

INSERT INTO atomic_command (
    id,
    name,
    command,
    role,
    skill_id,
    create_time,
    update_time,
    status,
    reserver,
    remark
)
SELECT
    'atomic-tool-safe-placeholder',
    '工具类安全占位',
    'TOOL_SAFE_PLACEHOLDER',
    'TOOL',
    '',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    '',
    '安全占位命令，不调用真实工具，等待白名单规则接入'
WHERE NOT EXISTS (
    SELECT 1
    FROM atomic_command
    WHERE id = 'atomic-tool-safe-placeholder'
);

-- ============================================================
-- 持久化聊天会话与消息迁移
-- ============================================================
CREATE TABLE IF NOT EXISTS agent_chat_session (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL DEFAULT '',
    session_name VARCHAR(255) NOT NULL DEFAULT '',
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS agent_chat_message (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL DEFAULT '',
    task_id VARCHAR(255) NOT NULL DEFAULT '',
    role VARCHAR(32) NOT NULL DEFAULT '',
    content TEXT NOT NULL,
    content_format VARCHAR(64) NOT NULL DEFAULT '',
    sequence_no BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT uk_agent_chat_message_session_sequence UNIQUE (session_id, sequence_no)
);

CREATE INDEX IF NOT EXISTS idx_agent_chat_session_agent_last_message
    ON agent_chat_session (agent_id, last_message_at DESC);

CREATE INDEX IF NOT EXISTS idx_agent_chat_message_session_sequence
    ON agent_chat_message (session_id, sequence_no);

CREATE INDEX IF NOT EXISTS idx_agent_chat_message_task
    ON agent_chat_message (task_id);

-- ============================================================
-- 多供应商/多模型运行时配置迁移
-- 执行顺序：先完成完整备份；再执行供应商和模型表DDL；随后追加引用与快照字段；
-- 最后创建索引。历史行仅补空快照，不根据YAML猜测供应商或密钥，历史记录保持可读。
-- 本段不执行任何真实迁移；非OPENAI_COMPATIBLE协议不得写入启用配置。
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_model_provider (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    provider_code VARCHAR(128) NOT NULL,
    provider_name VARCHAR(255) NOT NULL,
    protocol_type VARCHAR(64) NOT NULL,
    base_url VARCHAR(1000) NOT NULL,
    api_key_ciphertext TEXT NOT NULL,
    timeout_millis INTEGER NOT NULL DEFAULT 60000,
    system_default SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR(255) NOT NULL DEFAULT '',
    update_by VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT uk_ai_model_provider_code UNIQUE (provider_code)
);

CREATE TABLE IF NOT EXISTS ai_model (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    provider_id VARCHAR(255) NOT NULL,
    model_code VARCHAR(255) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    capability_config TEXT NOT NULL DEFAULT '',
    context_window INTEGER,
    provider_default SMALLINT NOT NULL DEFAULT 0,
    system_default SMALLINT NOT NULL DEFAULT 0,
    create_by VARCHAR(255) NOT NULL DEFAULT '',
    update_by VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT uk_ai_model_provider_code UNIQUE (provider_id, model_code)
);

ALTER TABLE agent_definition ADD COLUMN IF NOT EXISTS default_model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN IF NOT EXISTS provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN IF NOT EXISTS model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN IF NOT EXISTS model_code VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN IF NOT EXISTS provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN IF NOT EXISTS model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN IF NOT EXISTS model_code VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN IF NOT EXISTS provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN IF NOT EXISTS model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN IF NOT EXISTS model_code VARCHAR(255) NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_ai_model_provider_status
    ON ai_model_provider (status, provider_name);
CREATE INDEX IF NOT EXISTS idx_ai_model_provider_status_default
    ON ai_model (provider_id, status, provider_default);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_model_one_system_default
    ON ai_model (system_default) WHERE system_default = 1;
CREATE INDEX IF NOT EXISTS idx_agent_definition_default_model
    ON agent_definition (default_model_id);
CREATE INDEX IF NOT EXISTS idx_task_model_snapshot
    ON task (model_id, provider_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_task_detail_model_snapshot
    ON task_detail (model_id, provider_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_agent_chat_message_model_snapshot
    ON agent_chat_message (model_id, provider_id, create_time DESC);
