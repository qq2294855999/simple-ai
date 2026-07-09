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
