-- ============================================================
-- simple-ai 智能体设计图对齐版 PostgreSQL 建表脚本
-- 说明：已整合 SQL 二次复核中发现的缺失字段，枚举字段保留为 SMALLINT 或 VARCHAR，后续由 Java 枚举替换实体类型。
-- ============================================================

-- ============================================================
-- 1. 智能体定义
-- ============================================================
DROP TABLE IF EXISTS agent_definition;
CREATE TABLE agent_definition (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL DEFAULT '',
    definition_desc TEXT NOT NULL,
    first_principle TEXT NOT NULL,
    second_rule TEXT NOT NULL,
    third_skill TEXT NOT NULL,
    model VARCHAR(255) NOT NULL DEFAULT '',
    create_by VARCHAR(255) NOT NULL DEFAULT '',
    update_by VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE agent_definition IS '智能体定义';
COMMENT ON COLUMN agent_definition.id IS '主键';
COMMENT ON COLUMN agent_definition.name IS '名称';
COMMENT ON COLUMN agent_definition.definition_desc IS '定义描述';
COMMENT ON COLUMN agent_definition.first_principle IS '第一铁律';
COMMENT ON COLUMN agent_definition.second_rule IS '第二规则';
COMMENT ON COLUMN agent_definition.third_skill IS '第三技能';
COMMENT ON COLUMN agent_definition.model IS '模型';
COMMENT ON COLUMN agent_definition.create_by IS '创建人';
COMMENT ON COLUMN agent_definition.update_by IS '修改人';
COMMENT ON COLUMN agent_definition.create_time IS '创建时间';
COMMENT ON COLUMN agent_definition.update_time IS '修改时间';
COMMENT ON COLUMN agent_definition.status IS '状态';
COMMENT ON COLUMN agent_definition.reserver IS '扩展';
COMMENT ON COLUMN agent_definition.remark IS '备注';

-- ============================================================
-- 2. 智能体技能
-- ============================================================
DROP TABLE IF EXISTS agent_skill;
CREATE TABLE agent_skill (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL DEFAULT '',
    definition_desc TEXT NOT NULL,
    exec_content TEXT NOT NULL,
    return_data_format TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE agent_skill IS '智能体技能';
COMMENT ON COLUMN agent_skill.id IS '主键';
COMMENT ON COLUMN agent_skill.agent_id IS '智能体ID';
COMMENT ON COLUMN agent_skill.definition_desc IS '定义描述';
COMMENT ON COLUMN agent_skill.exec_content IS '执行内容';
COMMENT ON COLUMN agent_skill.return_data_format IS '返回的数据格式';
COMMENT ON COLUMN agent_skill.create_time IS '创建时间';
COMMENT ON COLUMN agent_skill.update_time IS '修改时间';
COMMENT ON COLUMN agent_skill.status IS '状态';
COMMENT ON COLUMN agent_skill.reserver IS '扩展';
COMMENT ON COLUMN agent_skill.remark IS '备注';

-- ============================================================
-- 3. 智能体规则
-- ============================================================
DROP TABLE IF EXISTS agent_rule;
CREATE TABLE agent_rule (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL DEFAULT '',
    definition_desc TEXT NOT NULL,
    trigger_condition TEXT NOT NULL,
    trigger_action TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE agent_rule IS '智能体规则';
COMMENT ON COLUMN agent_rule.id IS '主键';
COMMENT ON COLUMN agent_rule.agent_id IS '智能体ID';
COMMENT ON COLUMN agent_rule.definition_desc IS '定义描述';
COMMENT ON COLUMN agent_rule.trigger_condition IS '触发条件';
COMMENT ON COLUMN agent_rule.trigger_action IS '触发动作';
COMMENT ON COLUMN agent_rule.create_time IS '创建时间';
COMMENT ON COLUMN agent_rule.update_time IS '修改时间';
COMMENT ON COLUMN agent_rule.status IS '状态';
COMMENT ON COLUMN agent_rule.reserver IS '扩展';
COMMENT ON COLUMN agent_rule.remark IS '备注';

-- ============================================================
-- 4. 子智能体关联
-- ============================================================
DROP TABLE IF EXISTS sub_agent_relation;
CREATE TABLE sub_agent_relation (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    main_agent_id VARCHAR(255) NOT NULL DEFAULT '',
    sub_agent_id VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE sub_agent_relation IS '子智能体关联';
COMMENT ON COLUMN sub_agent_relation.id IS '主键';
COMMENT ON COLUMN sub_agent_relation.main_agent_id IS '主智能体';
COMMENT ON COLUMN sub_agent_relation.sub_agent_id IS '子智能体';
COMMENT ON COLUMN sub_agent_relation.create_time IS '创建时间';
COMMENT ON COLUMN sub_agent_relation.update_time IS '修改时间';
COMMENT ON COLUMN sub_agent_relation.status IS '状态';
COMMENT ON COLUMN sub_agent_relation.reserver IS '扩展';
COMMENT ON COLUMN sub_agent_relation.remark IS '备注';

-- ============================================================
-- 5. 智能体记忆
-- ============================================================
DROP TABLE IF EXISTS agent_memory;
CREATE TABLE agent_memory (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL DEFAULT '',
    memory_name VARCHAR(255) NOT NULL DEFAULT '',
    step_name VARCHAR(255) NOT NULL DEFAULT '',
    trigger_condition TEXT NOT NULL,
    trigger_action TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE agent_memory IS '智能体记忆';
COMMENT ON COLUMN agent_memory.id IS '主键';
COMMENT ON COLUMN agent_memory.agent_id IS '智能体ID';
COMMENT ON COLUMN agent_memory.memory_name IS '记忆名称';
COMMENT ON COLUMN agent_memory.step_name IS '步骤名称';
COMMENT ON COLUMN agent_memory.trigger_condition IS '触发条件';
COMMENT ON COLUMN agent_memory.trigger_action IS '触发动作';
COMMENT ON COLUMN agent_memory.create_time IS '创建时间';
COMMENT ON COLUMN agent_memory.update_time IS '修改时间';
COMMENT ON COLUMN agent_memory.status IS '状态';
COMMENT ON COLUMN agent_memory.reserver IS '扩展';
COMMENT ON COLUMN agent_memory.remark IS '备注';

-- ============================================================
-- 6. 智能体记忆详情
-- ============================================================
DROP TABLE IF EXISTS agent_memory_detail;
CREATE TABLE agent_memory_detail (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_memory_id VARCHAR(255) NOT NULL DEFAULT '',
    step_name VARCHAR(255) NOT NULL DEFAULT '',
    step_type VARCHAR(255) NOT NULL DEFAULT '',
    exec_content TEXT NOT NULL,
    return_data_format TEXT NOT NULL,
    parent_step_id VARCHAR(255) NOT NULL DEFAULT '',
    next_step_id VARCHAR(255) NOT NULL DEFAULT '',
    branch_condition TEXT NOT NULL,
    branch_route TEXT NOT NULL,
    model VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE agent_memory_detail IS '智能体记忆详情';
COMMENT ON COLUMN agent_memory_detail.id IS '主键';
COMMENT ON COLUMN agent_memory_detail.agent_memory_id IS '智能体记忆ID';
COMMENT ON COLUMN agent_memory_detail.step_name IS '步骤名称';
COMMENT ON COLUMN agent_memory_detail.step_type IS '步骤类型：智能体记忆步骤类型';
COMMENT ON COLUMN agent_memory_detail.exec_content IS '执行内容';
COMMENT ON COLUMN agent_memory_detail.return_data_format IS '返回的数据格式';
COMMENT ON COLUMN agent_memory_detail.parent_step_id IS '父步骤ID';
COMMENT ON COLUMN agent_memory_detail.next_step_id IS '下一个步骤ID';
COMMENT ON COLUMN agent_memory_detail.branch_condition IS '分支条件';
COMMENT ON COLUMN agent_memory_detail.branch_route IS '分支路由';
COMMENT ON COLUMN agent_memory_detail.model IS '模型';
COMMENT ON COLUMN agent_memory_detail.create_time IS '创建时间';
COMMENT ON COLUMN agent_memory_detail.update_time IS '修改时间';
COMMENT ON COLUMN agent_memory_detail.status IS '状态';
COMMENT ON COLUMN agent_memory_detail.reserver IS '扩展';
COMMENT ON COLUMN agent_memory_detail.remark IS '备注';

-- ============================================================
-- 7. 原子命令
-- ============================================================
DROP TABLE IF EXISTS atomic_command;
CREATE TABLE atomic_command (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL DEFAULT '',
    command TEXT NOT NULL,
    role TEXT NOT NULL,
    skill_id VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE atomic_command IS '原子命令';
COMMENT ON COLUMN atomic_command.id IS '主键';
COMMENT ON COLUMN atomic_command.name IS '名称';
COMMENT ON COLUMN atomic_command.command IS '命令';
COMMENT ON COLUMN atomic_command.role IS '作用';
COMMENT ON COLUMN atomic_command.skill_id IS '智能体技能ID';
COMMENT ON COLUMN atomic_command.create_time IS '创建时间';
COMMENT ON COLUMN atomic_command.update_time IS '修改时间';
COMMENT ON COLUMN atomic_command.status IS '状态';
COMMENT ON COLUMN atomic_command.reserver IS '扩展';
COMMENT ON COLUMN atomic_command.remark IS '备注';

-- ============================================================
-- 8. 任务
-- ============================================================
DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    agent_memory_id VARCHAR(255) NOT NULL DEFAULT '',
    agent_id VARCHAR(255) NOT NULL DEFAULT '',
    task_name VARCHAR(255) NOT NULL DEFAULT '',
    parent_task_id VARCHAR(255) NOT NULL DEFAULT '',
    next_task_id VARCHAR(255) NOT NULL DEFAULT '',
    step_type VARCHAR(255) NOT NULL DEFAULT '',
    branch_condition TEXT NOT NULL,
    branch_route TEXT NOT NULL,
    request_params TEXT NOT NULL,
    return_params TEXT NOT NULL,
    exec_status VARCHAR(255) NOT NULL DEFAULT '',
    failure_reason TEXT NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE task IS '任务';
COMMENT ON COLUMN task.id IS '主键';
COMMENT ON COLUMN task.agent_memory_id IS '智能体记忆主键';
COMMENT ON COLUMN task.agent_id IS '智能体主键';
COMMENT ON COLUMN task.task_name IS '任务名称';
COMMENT ON COLUMN task.parent_task_id IS '父任务ID';
COMMENT ON COLUMN task.next_task_id IS '下一个任务ID';
COMMENT ON COLUMN task.step_type IS '步骤类型：智能体步骤类型';
COMMENT ON COLUMN task.branch_condition IS '分支条件';
COMMENT ON COLUMN task.branch_route IS '分支路由';
COMMENT ON COLUMN task.request_params IS '请求参数';
COMMENT ON COLUMN task.return_params IS '返回参数';
COMMENT ON COLUMN task.exec_status IS '执行状态';
COMMENT ON COLUMN task.failure_reason IS '失败原因';
COMMENT ON COLUMN task.create_time IS '创建时间';
COMMENT ON COLUMN task.update_time IS '修改时间';
COMMENT ON COLUMN task.status IS '状态';
COMMENT ON COLUMN task.reserver IS '扩展';
COMMENT ON COLUMN task.remark IS '备注';

-- ============================================================
-- 9. 任务详情
-- ============================================================
DROP TABLE IF EXISTS task_detail;
CREATE TABLE task_detail (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL DEFAULT '',
    task_name VARCHAR(255) NOT NULL DEFAULT '',
    parent_task_id VARCHAR(255) NOT NULL DEFAULT '',
    next_task_id VARCHAR(255) NOT NULL DEFAULT '',
    step_type VARCHAR(255) NOT NULL DEFAULT '',
    branch_condition TEXT NOT NULL,
    branch_route TEXT NOT NULL,
    request_params TEXT NOT NULL,
    return_params TEXT NOT NULL,
    exec_status VARCHAR(255) NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1,
    reserver TEXT,
    remark VARCHAR(500) NOT NULL DEFAULT ''
);
COMMENT ON TABLE task_detail IS '任务详情';
COMMENT ON COLUMN task_detail.id IS '主键';
COMMENT ON COLUMN task_detail.task_id IS '任务主键';
COMMENT ON COLUMN task_detail.task_name IS '任务名称';
COMMENT ON COLUMN task_detail.parent_task_id IS '父任务ID';
COMMENT ON COLUMN task_detail.next_task_id IS '下一个任务ID';
COMMENT ON COLUMN task_detail.step_type IS '步骤类型：智能体步骤类型';
COMMENT ON COLUMN task_detail.branch_condition IS '分支条件';
COMMENT ON COLUMN task_detail.branch_route IS '分支路由';
COMMENT ON COLUMN task_detail.request_params IS '请求参数';
COMMENT ON COLUMN task_detail.return_params IS '返回参数';
COMMENT ON COLUMN task_detail.exec_status IS '执行状态';
COMMENT ON COLUMN task_detail.create_time IS '创建时间';
COMMENT ON COLUMN task_detail.update_time IS '修改时间';
COMMENT ON COLUMN task_detail.status IS '状态';
COMMENT ON COLUMN task_detail.reserver IS '扩展';
COMMENT ON COLUMN task_detail.remark IS '备注';

-- ============================================================
-- 10. 智能体聊天会话
-- ============================================================
CREATE TABLE agent_chat_session (
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
COMMENT ON TABLE agent_chat_session IS '智能体聊天会话';
COMMENT ON COLUMN agent_chat_session.id IS '主键';
COMMENT ON COLUMN agent_chat_session.agent_id IS '绑定智能体主键';
COMMENT ON COLUMN agent_chat_session.session_name IS '会话名称';
COMMENT ON COLUMN agent_chat_session.last_message_at IS '最后消息时间';

-- ============================================================
-- 11. 智能体聊天消息
-- ============================================================
CREATE TABLE agent_chat_message (
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
COMMENT ON TABLE agent_chat_message IS '智能体聊天消息';
COMMENT ON COLUMN agent_chat_message.session_id IS '聊天会话主键';
COMMENT ON COLUMN agent_chat_message.task_id IS '关联调度任务主键';
COMMENT ON COLUMN agent_chat_message.role IS '消息角色：USER、ASSISTANT、SYSTEM_ERROR';
COMMENT ON COLUMN agent_chat_message.content IS '消息内容';
COMMENT ON COLUMN agent_chat_message.content_format IS '内容格式：PLAIN_TEXT、RESTRICTED_MARKDOWN';
-- ============================================================
-- 12. 模型供应商配置
-- ============================================================
CREATE TABLE ai_model_provider (
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
COMMENT ON TABLE ai_model_provider IS 'AI模型供应商运行配置，API Key仅保存AES-GCM密文';
COMMENT ON COLUMN ai_model_provider.protocol_type IS '协议类型，首期仅支持OPENAI_COMPATIBLE';
COMMENT ON COLUMN ai_model_provider.api_key_ciphertext IS 'API Key AES-GCM加密密文，禁止回显、日志与审计复制';
COMMENT ON COLUMN ai_model_provider.system_default IS '是否系统默认供应商，仅辅助运维展示；实际默认由模型表确定';

-- ============================================================
-- 13. 模型配置
-- ============================================================
CREATE TABLE ai_model (
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
COMMENT ON TABLE ai_model IS 'AI模型配置，关联供应商且不重复存储API Key';
COMMENT ON COLUMN ai_model.capability_config IS '可扩展能力JSON文本，例如chat、vision、functionCalling';
COMMENT ON COLUMN ai_model.provider_default IS '供应商默认模型';
COMMENT ON COLUMN ai_model.system_default IS '系统默认模型，全局仅允许一个启用模型';

-- ============================================================
-- 14. 运行时模型选择与审计快照
-- ============================================================
ALTER TABLE agent_definition ADD COLUMN default_model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task ADD COLUMN model_code VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE task_detail ADD COLUMN model_code VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN provider_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN provider_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN model_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE agent_chat_message ADD COLUMN model_code VARCHAR(255) NOT NULL DEFAULT '';

CREATE INDEX idx_ai_model_provider_status ON ai_model_provider (status, provider_name);
CREATE INDEX idx_ai_model_provider_status_default ON ai_model (provider_id, status, provider_default);
CREATE UNIQUE INDEX uk_ai_model_one_system_default ON ai_model (system_default) WHERE system_default = 1;
CREATE INDEX idx_agent_definition_default_model ON agent_definition (default_model_id);
CREATE INDEX idx_task_model_snapshot ON task (model_id, provider_id, create_time DESC);
CREATE INDEX idx_task_detail_model_snapshot ON task_detail (model_id, provider_id, create_time DESC);
CREATE INDEX idx_agent_chat_message_model_snapshot ON agent_chat_message (model_id, provider_id, create_time DESC);
