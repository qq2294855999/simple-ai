-- ============================================================
-- simple-ai 智能体命令调度运行辅助 PostgreSQL 脚本
-- 说明：本脚本不修改核心表结构，仅补充调度链路常用索引与安全占位原子命令样例。
-- ============================================================

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
CREATE INDEX IF NOT EXISTS idx_atomic_command_status
    ON atomic_command (status);

CREATE INDEX IF NOT EXISTS idx_atomic_command_name_status
    ON atomic_command (name, status);

CREATE INDEX IF NOT EXISTS idx_task_parent_status
    ON task (parent_task_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_memory_status
    ON task (agent_memory_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_detail_task_status
    ON task_detail (task_id, exec_status);

CREATE INDEX IF NOT EXISTS idx_task_detail_parent_next
    ON task_detail (parent_task_id, next_task_id);

-- ============================================================
-- 安全占位原子命令样例
-- ============================================================
INSERT INTO atomic_command (
    id,
    name,
    command,
    role,
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
