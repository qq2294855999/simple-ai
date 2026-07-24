# 记忆体系重构 - 设计方案

## 当前恢复入口

- 当前阶段：全部完成。
- 当前状态：26/26 步骤已全部完成，mvn clean package 编译通过。
- 下一步：前端页面开发（如需）。

## 用户确认决策

| 决策项       | 结论                                            |
|--------------|-------------------------------------------------|
| 整体方案     | 统一方案：删除5旧表+全套代码，新增2表+4核心服务 |
| 蒸馏AI模型   | 复用当前会话的模型，不单独配置                  |
| 记忆匹配策略 | 调用AI做意图识别匹配已发布记忆                  |

---

## 一、需求背景

### 1.1 业务场景

用户通过人机对话探索"未知的新流程"。例如，用户说"向XXX发送你好啊三个字"，智能体需要：

1. 根据当前会话（智能体 + 大模型 + 客户端）获取客户端信息
2. 根据客户端协议获取支持的原子命令
3. 逐步调用原子命令完成任务（探索过程）
4. 探索成功后，将最短成功路径沉淀为 **记忆**
5. 后续可直接根据记忆发起任务，无需智能体介入
6. 若记忆执行失败，智能体重新介入，修订失败方案，生成新版本记忆

### 1.2 核心概念

```
记忆 = 模板（存什么）    任务 = 实例（跑什么）
agent_memory            task
  └ agent_memory_step     └ task_detail
  （原子命令 + 参数模板）  （实际参数 + 执行结果）

版本化迭代               每次执行创建一个任务
DRAFT→PUBLISHED→RETIRED  RUNNING→SUCCESS/FAILED
```

### 1.3 SQL 生成强制约束

> **铁律**：本方案涉及数据库表结构变动（新增/删除表、增加/删除/修改字段），所有 SQL 生成必须使用 `db-ddl-generator` 技能，严格按照该技能规范生成 DDL，禁止手动编写建表语句。

### 1.4 整体设计思路

> 本节记录本轮对话的完整设计推演过程，供后续开发会话理解设计意图。

#### 1.4.1 核心哲学

人机对话的本质是"探索未知的新流程"。用户告诉智能体一个目标，智能体在客户端环境中逐步探索原子命令，找到成功路径后沉淀为记忆。后续可直接根据记忆发起任务，无需智能体再次介入。

**三个关键角色：**

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   智能体       │    │   记忆         │    │   任务         │
│   (Agent)     │    │   (Memory)    │    │   (Task)      │
├──────────────┤    ├──────────────┤    ├──────────────┤
│ 探索未知流程   │ →  │ 沉淀成功路径   │ →  │ 复用执行       │
│ 发现原子命令   │    │ 存储参数模板   │    │ 创建实例       │
│ 逐步试错       │    │ 版本化迭代     │    │ 记录执行结果   │
│ 失败后修订     │ ←  │ 记忆失败触发   │ ←  │ 执行失败       │
└──────────────┘    └──────────────┘    └──────────────┘
```

#### 1.4.2 设计决策记录

| 决策             | 结论                              | 理由                                                                       |
|------------------|-----------------------------------|----------------------------------------------------------------------------|
| 记忆与任务的关系 | 模板与实例                        | 记忆存"做什么"（原子命令+参数模板），任务存"做了什么"（实际参数+执行结果） |
| 记忆版本化       | 同一 memory 下 version_no 递增    | 每次修订产生新版本，旧版本可保留/淘汰，不影响已创建的任务                  |
| 参数化方案       | 占位符 `{param_name}` + JSON 定义 | AI 蒸馏时自动识别变量，执行时用户传入参数，前端动态生成表单                |
| 旧表处理         | 全部删除，不做兼容                | 开发阶段，旧设计有两套并行+孤儿表，直接推翻重来                            |
| 蒸馏时机         | AI 探索成功、任务完成后           | 读取 task + task_details，AI 识别最短路径和参数，创建 DRAFT 记忆           |
| 直执行失败处理   | 智能体重新介入，修订记忆          | 失败后创建新版本记忆（MEMORY_REVISE），而非直接修补当前版本                |

#### 1.4.3 参数化设计

记忆模板支持占位符变量，蒸馏时 AI 自动识别，执行时用户传入参数替换。动态参数以 `params_definition`（JSON 对象）形式存储，前端以表单集合呈现，后端以 Map 形式接收。

```
蒸馏时 AI 识别：
  用户意图："向XXX发送AAA消息"
  实际命令：weixin_search_contact:文件传输助手 / weixin_send_message:你好啊
  → 识别参数：{contact_name}、{message_content}
  → 记忆名称："向{contact_name}发送{message_content}"
  → 步骤模板：{"contact_name": "{contact_name}"}

执行时参数替换：
  用户输入：memoryId + {"contact_name": "张三", "message_content": "在吗"}
  → 步骤1：{"contact_name": "张三"} → weixin_search_contact:张三
  → 步骤2：{"message_content": "在吗"} → weixin_send_message:在吗
```

---

## 二、当前问题诊断

### 2.1 现有记忆体系（5 张表，两套并行 + 一个孤儿）

```
┌── 旧设计（规则匹配）──────────┐  ┌── 新设计（版本化）──────────────┐  ┌── 孤儿 ──┐
│ agent_memory                  │  │ agent_memory_version            │  │ memory_  │
│   ├ trigger_condition 文本匹配│  │   ├ version_no, version_status  │  │ evidence │
│   └ agent_memory_detail       │  │   └ agent_memory_version_detail │  │ (无人读) │
│     ├ parent_step_id 链表     │  │     ├ atomic_command_id 直连    │  └──────────┘
│     ├ next_step_id 跳转       │  │     ├ sequence_no 顺序         │
│     └ branch_condition 分支   │  │     └ args_template 参数模板    │
└───────────────────────────────┘  └─────────────────────────────────┘
```

### 2.2 具体问题

| 问题                           | 说明                                                                                                          |
|--------------------------------|---------------------------------------------------------------------------------------------------------------|
| 两套并行设计                   | `executeMemorySteps()` 实际跑的是旧设计（agent_memory_detail），新设计（agent_memory_version_detail）未被使用 |
| `memory_evidence` 孤儿         | `DefaultAgentMemoryDistiller` 只写不读，`memory_version_id` 永远为空字符串                                    |
| 记忆沉淀未实现                 | `triggerMemoryPrecipitation()` 是 TODO                                                                        |
| `AgentMemorySummarizer` 未调用 | 定义了但从未被使用                                                                                            |
| 记忆无法直执行                 | 当前记忆命中后仍走 AI 流程，没有"从记忆创建任务并直接下发客户端"的路径                                        |
| 无参数化能力                   | 旧设计中具体值硬编码，无法复用                                                                                |

---

## 三、设计方案

### 3.1 两条核心业务路径

```
┌──────────────────────────────────────────────────────────────────┐
│ 路径A：AI 探索 → 沉淀记忆                                         │
│                                                                  │
│  用户: "向XXX发送你好啊"                                           │
│    ↓                                                             │
│  AgentChatService.sendStream()                                    │
│    ↓                                                             │
│  CommandDispatchService.dispatchStream()                          │
│    ├─ AgentContextAssembler: 装配上下文（智能体+技能+原子命令）     │
│    ├─ MemoryMatcher: 匹配已发布记忆 → 未命中                        │
│    ├─ AgentAiClient: AI 逐步探索                                   │
│    │   ├─ 第1步: 创建原子命令"打开微信" → 成功                      │
│    │   ├─ 第2步: 创建原子命令"搜索联系人" → 成功                    │
│    │   └─ 第3步: 创建原子命令"发送消息" → 成功                      │
│    └─ AI 返回最终结果                                              │
│    ↓                                                             │
│  MemoryDistiller.distill(taskId)                                  │
│    ├─ 读取 task + task_details                                    │
│    ├─ 调用 AI 识别参数（识别变量占位符）                            │
│    ├─ 创建 agent_memory (DRAFT)                                    │
│    │   memory_name = "向{contact_name}发送{message_content}"       │
│    │   params_definition = {contact_name, message_content}         │
│    │   create_reason = AI_EXPLORATION                              │
│    └─ 创建 agent_memory_step × N                                   │
│        args_template = {"contact_name": "{contact_name}"}          │
│    ↓                                                             │
│  用户在前端确认 → publish → PUBLISHED                              │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ 路径B：记忆直执行                                                  │
│                                                                  │
│  用户选择记忆 + 传入参数                                           │
│    memoryId = "向{contact_name}发送{message_content}"             │
│    params = {"contact_name": "张三", "message_content": "在吗"}    │
│    ↓                                                             │
│  MemoryExecutor.execute(memoryId, params)                         │
│    ├─ 加载 agent_memory + agent_memory_steps                      │
│    ├─ 校验 params 与 params_definition 匹配                        │
│    ├─ 替换 args_template 占位符 → actual_args                      │
│    ├─ 创建 task（memory_id + memory_version_no 快照）              │
│    ├─ 创建 task_detail × N（从 memory_step 复制，填充 actual_args）│
│    └─ WebSocket 下发客户端执行                                     │
│    ↓                                                             │
│  客户端逐条执行并回传结果                                           │
│    ├─ 全部成功 → task.exec_status = SUCCESS                       │
│    └─ 某条失败 → task.exec_status = FAILED                         │
│       ↓                                                          │
│       触发智能体介入 → 修订 → 新版本 agent_memory                   │
│       (create_reason = MEMORY_REVISE, version_no + 1)             │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 新表结构

#### agent_memory（记忆模板）

```sql
CREATE TABLE agent_memory
(
    id                VARCHAR(32)  NOT NULL,           -- 主键，雪花ID
    agent_id          VARCHAR(32)  NOT NULL,           -- 所属智能体ID
    memory_name       VARCHAR(255) NOT NULL,           -- 记忆名称模板，如"向{contact_name}发送{message_content}"
    params_definition JSONB,                           -- 参数定义，描述可变参数的类型和含义
    version_no        INT4         NOT NULL DEFAULT 1, -- 版本号，同一记忆下从1递增
    version_status    INT2         NOT NULL DEFAULT 1, -- 1-DRAFT(草稿) / 2-PUBLISHED(已发布) / 3-RETIRED(已淘汰)
    source_task_id    VARCHAR(32),                     -- 来源任务ID，记录该版本由哪个任务产生
    summary           TEXT,                            -- AI生成的该版本记忆摘要
    create_reason     VARCHAR(64),                     -- 创建原因: AI_EXPLORATION(AI探索沉淀) / MEMORY_REVISE(失败修订)
    client_id         VARCHAR(32),                     -- 关联的客户端ID（执行器实例）
    user_id           VARCHAR(32),                     -- 用户归属ID，确保记忆私域隔离
    create_user_id    VARCHAR(32),                     -- 创建人用户ID
    create_time       TIMESTAMP(6),
    update_time       TIMESTAMP(6),
    status            INT2         NOT NULL DEFAULT 1, -- ON(启用) / DISABLE(停用)
    PRIMARY KEY (id)
);

COMMENT
ON TABLE agent_memory IS '智能体记忆模板';
COMMENT
ON COLUMN agent_memory.id IS '记忆主键';
COMMENT
ON COLUMN agent_memory.agent_id IS '所属智能体ID';
COMMENT
ON COLUMN agent_memory.memory_name IS '记忆名称模板，支持{param}占位符';
COMMENT
ON COLUMN agent_memory.params_definition IS '参数定义JSON，描述每个占位符的类型和含义';
COMMENT
ON COLUMN agent_memory.version_no IS '版本号，从1递增';
COMMENT
ON COLUMN agent_memory.version_status IS '1-DRAFT / 2-PUBLISHED / 3-RETIRED';
COMMENT
ON COLUMN agent_memory.source_task_id IS '来源任务ID';
COMMENT
ON COLUMN agent_memory.summary IS 'AI生成的记忆摘要';
COMMENT
ON COLUMN agent_memory.create_reason IS 'AI_EXPLORATION / MEMORY_REVISE';
COMMENT
ON COLUMN agent_memory.client_id IS '关联客户端ID';
COMMENT
ON COLUMN agent_memory.user_id IS '用户归属ID';
COMMENT
ON COLUMN agent_memory.create_user_id IS '创建人用户ID';
COMMENT
ON COLUMN agent_memory.status IS 'ON(启用) / DISABLE(停用)';
```

#### params_definition 示例

```json
{
	"contact_name": {
		"type": "string",
		"description": "收件人名称，如'文件传输助手'、'张三'"
	},
	"message_content": {
		"type": "string",
		"description": "要发送的消息内容"
	}
}
```

#### agent_memory_step（记忆步骤）

```sql
CREATE TABLE agent_memory_step
(
    id                  VARCHAR(32)  NOT NULL,      -- 主键
    memory_id           VARCHAR(32)  NOT NULL,      -- 关联 agent_memory.id
    sequence_no         INT4         NOT NULL,      -- 步骤序号，从10开始递增，决定执行顺序
    atomic_command_id   VARCHAR(32)  NOT NULL,      -- 原子命令主键，关联 atomic_command.id
    atomic_command_code VARCHAR(128) NOT NULL,      -- 原子命令编码（冗余），如 weixin_search_contact
    step_name           VARCHAR(255) NOT NULL,      -- 步骤名称，如"搜索联系人"
    args_template       JSONB,                      -- 参数模板JSON，运行时替换后传给执行器
    delay_min_ms        INT4        DEFAULT 100,    -- 执行前随机延迟最小值（毫秒）
    delay_max_ms        INT4        DEFAULT 500,    -- 执行前随机延迟最大值（毫秒）
    timeout_ms          INT4        DEFAULT 30000,  -- 命令超时时间（毫秒）
    success_assertion   TEXT,                       -- 成功断言规则，用于判断该步骤是否执行成功
    failure_strategy    VARCHAR(32) DEFAULT 'STOP', -- 失败处理策略: STOP(停止) / RETRY(重试) / SKIP(跳过)
    status              VARCHAR(16) DEFAULT 'ON',   -- ON(启用) / OFF(停用)
    create_time         TIMESTAMP(6),
    update_time         TIMESTAMP(6),
    PRIMARY KEY (id)
);

COMMENT
ON TABLE agent_memory_step IS '记忆步骤';
COMMENT
ON COLUMN agent_memory_step.id IS '步骤主键';
COMMENT
ON COLUMN agent_memory_step.memory_id IS '关联的记忆ID';
COMMENT
ON COLUMN agent_memory_step.sequence_no IS '步骤序号，从10递增';
COMMENT
ON COLUMN agent_memory_step.atomic_command_id IS '原子命令主键';
COMMENT
ON COLUMN agent_memory_step.atomic_command_code IS '原子命令编码（冗余）';
COMMENT
ON COLUMN agent_memory_step.step_name IS '步骤名称';
COMMENT
ON COLUMN agent_memory_step.args_template IS '参数模板JSON，支持{param}占位符';
COMMENT
ON COLUMN agent_memory_step.delay_min_ms IS '执行前延迟最小值';
COMMENT
ON COLUMN agent_memory_step.delay_max_ms IS '执行前延迟最大值';
COMMENT
ON COLUMN agent_memory_step.timeout_ms IS '命令超时时间';
COMMENT
ON COLUMN agent_memory_step.success_assertion IS '成功断言规则';
COMMENT
ON COLUMN agent_memory_step.failure_strategy IS 'STOP / RETRY / SKIP';
COMMENT
ON COLUMN agent_memory_step.status IS 'ON(启用) / OFF(停用)';
```

#### args_template 示例

```json
// 步骤1：搜索联系人
{
	"contact_name": "{contact_name}"
}

// 步骤2：发送消息
{
	"message_content": "{message_content}"
}
```

### 3.3 修改现有表

#### task 表

```diff
- agent_memory_id    VARCHAR(255)   -- 删除（引用旧 agent_memory 表）
- memory_version_id  VARCHAR(255)   -- 删除（引用旧 agent_memory_version 表）
+ memory_id          VARCHAR(32)    -- 新增，关联新 agent_memory.id
+ memory_version_no  INT4           -- 新增，快照执行时的记忆版本号
```

---

## 四、变更清单

### 4.1 删除（5 张表 + 相关代码文件）

| 删除项                        | 类型 | 原因                                                                |
|-------------------------------|------|---------------------------------------------------------------------|
| `agent_memory`                | 表   | 旧设计，trigger_condition 文本匹配，被新 agent_memory 替代          |
| `agent_memory_detail`         | 表   | 旧设计，parent_step_id/next_step_id 链表，被 agent_memory_step 替代 |
| `agent_memory_version`        | 表   | 层级冗余（memory→version→detail 三层），合并到新 agent_memory       |
| `agent_memory_version_detail` | 表   | 字段设计可用，重命名为 agent_memory_step                            |
| `memory_evidence`             | 表   | 孤儿表，只写不读，数据可从 execution_event 实时聚合                 |

连带删除的 Java 文件目录：

| 目录/文件                                                 | 说明                                        |
|-----------------------------------------------------------|---------------------------------------------|
| `common/entity/agentMemory/`                              | AgentMemory.java（旧）                      |
| `common/entity/agentMemoryDetail/`                        | AgentMemoryDetail.java（旧）                |
| `common/entity/agentMemoryVersion/`                       | AgentMemoryVersion.java（旧）               |
| `common/entity/agentMemoryVersionDetail/`                 | AgentMemoryVersionDetail.java（旧）         |
| `common/entity/memoryEvidence/`                           | MemoryEvidence.java                         |
| `common/dto/agentMemory/`                                 | 旧 DTO 全套                                 |
| `common/dto/agentMemoryDetail/`                           | 旧 DTO 全套                                 |
| `common/dto/agentMemoryVersion/`                          | 旧 DTO 全套                                 |
| `common/dto/agentMemoryVersionDetail/`                    | 旧 DTO 全套                                 |
| `common/dto/memoryEvidence/`                              | DTO 全套                                    |
| `common/service/agentMemory/`                             | 旧 Service 接口                             |
| `common/service/agentMemoryDetail/`                       | 旧 Service 接口                             |
| `common/service/agentMemoryVersion/`                      | 旧 Service 接口                             |
| `common/service/memoryEvidence/`                          | Service 接口                                |
| `common/service/executionEvent/AgentMemoryDistiller.java` | 接口                                        |
| `common/view/agentMemory/`                                | 旧 View 接口                                |
| `common/view/agentMemoryDetail/`                          | 旧 View 接口                                |
| `common/view/agentMemoryVersion/`                         | 旧 View 接口                                |
| `common/view/agentMemoryVersionDetail/`                   | 旧 View 接口                                |
| `common/view/memoryEvidence/`                             | View 接口                                   |
| `common/copy/agentMemory/`                                | 旧 CopyMapper                               |
| `common/copy/agentMemoryDetail/`                          | 旧 CopyMapper                               |
| `common/copy/agentMemoryVersion/`                         | 旧 CopyMapper                               |
| `common/copy/agentMemoryVersionDetail/`                   | 旧 CopyMapper                               |
| `common/copy/memoryEvidence/`                             | CopyMapper                                  |
| `controller/agentMemory/`                                 | AgentMemoryController.java（旧）            |
| `controller/agentMemoryDetail/`                           | AgentMemoryDetailController.java（旧）      |
| `controller/agentMemoryVersion/`                          | AgentMemoryVersionController.java（旧）     |
| `view/agentMemory/`                                       | 旧 Mapper/Repository                        |
| `view/agentMemoryDetail/`                                 | 旧 Mapper/Repository                        |
| `view/agentMemoryVersion/`                                | 旧 Mapper/Repository                        |
| `view/agentMemoryVersionDetail/`                          | 旧 Mapper/Repository                        |
| `view/memoryEvidence/`                                    | Mapper/Repository                           |
| `service/agentMemory/`                                    | DefaultAgentMemoryService.java（旧）        |
| `service/agentMemoryDetail/`                              | DefaultAgentMemoryDetailService.java（旧）  |
| `service/agentMemoryVersion/`                             | DefaultAgentMemoryVersionService.java（旧） |
| `service/memoryEvidence/`                                 | DefaultMemoryEvidenceService.java           |
| `service/executionEvent/DefaultAgentMemoryDistiller.java` | 实现                                        |
| `service/agent/AgentMemoryMatcher.java`                   | 旧匹配器                                    |
| `service/agent/AgentMemorySummarizer.java`                | 未调用                                      |

### 4.2 新增（2 张表 + 相关代码）

| 新增项              | 类型 | 说明                                  |
|---------------------|------|---------------------------------------|
| `agent_memory`      | 表   | 新记忆模板表，含 params_definition    |
| `agent_memory_step` | 表   | 新记忆步骤表，含 args_template 占位符 |

新增 Java 代码：

| 类                              | 路径                              | 职责                                                    |
|---------------------------------|-----------------------------------|---------------------------------------------------------|
| `AgentMemory`                   | `common/entity/agentMemory/`      | 新记忆实体                                              |
| `AgentMemoryStep`               | `common/entity/agentMemoryStep/`  | 新记忆步骤实体                                          |
| `AgentMemoryController`         | `controller/agentMemory/`         | 记忆 CRUD + publish/retire                              |
| `AgentMemoryStepController`     | `controller/agentMemoryStep/`     | 记忆步骤 CRUD                                           |
| `AgentMemoryService`            | `common/service/agentMemory/`     | 记忆服务接口                                            |
| `DefaultAgentMemoryService`     | `service/agentMemory/`            | 记忆服务实现                                            |
| `AgentMemoryStepService`        | `common/service/agentMemoryStep/` | 步骤服务接口                                            |
| `DefaultAgentMemoryStepService` | `service/agentMemoryStep/`        | 步骤服务实现                                            |
| `MemoryDistiller`               | `common/service/memory/`          | **核心**：蒸馏接口                                      |
| `DefaultMemoryDistiller`        | `service/memory/`                 | **核心**：从成功 task 提炼记忆（含 AI 参数识别）        |
| `MemoryExecutor`                | `common/service/memory/`          | **核心**：直执行接口                                    |
| `DefaultMemoryExecutor`         | `service/memory/`                 | **核心**：从 memory 创建 task + 占位符替换 + 下发客户端 |
| `MemoryMatcher`                 | `service/memory/`                 | **核心**：根据用户输入匹配已发布记忆                    |
| 配套 DTO                        | `common/dto/agentMemory/`         | 请求/响应 DTO                                           |
| 配套 DTO                        | `common/dto/agentMemoryStep/`     | 请求/响应 DTO                                           |
| 配套 View                       | `common/view/agentMemory/`        | 视图接口                                                |
| 配套 View                       | `common/view/agentMemoryStep/`    | 视图接口                                                |
| 配套 CopyMapper                 | `common/copy/agentMemory/`        | 实体转换                                                |
| 配套 CopyMapper                 | `common/copy/agentMemoryStep/`    | 实体转换                                                |
| 配套 Repository                 | `view/agentMemory/`               | MyBatis-Plus Repository                                 |
| 配套 Repository                 | `view/agentMemoryStep/`           | MyBatis-Plus Repository                                 |
| 配套 Mapper XML                 | `resources/mapper/`               | SQL 映射文件                                            |

### 4.3 修改（1 张表 + 少量代码）

| 修改项                          | 说明                                                                                                                                        |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `task` 表                       | 删除 `agent_memory_id`、`memory_version_id`，新增 `memory_id`、`memory_version_no`                                                          |
| `Task.java`                     | 同步修改实体字段                                                                                                                            |
| `task` 相关 DTO                 | 同步修改 DTO 字段                                                                                                                           |
| `DefaultCommandDispatchService` | 删除旧 AgentMemoryMatcher 引用；删除 executeMemorySteps() 旧逻辑；删除 triggerMemoryPrecipitation() TODO；AI 探索成功后回调 MemoryDistiller |
| `DefaultAgentChatService`       | 删除 AgentMemoryDistiller 引用；删除 triggerDistillation()；聊天完成后调用 MemoryDistiller.distill()                                        |
| `AgentContextAssembler`         | 删除旧 AgentMemoryMatcher 引用，改为加载新 memory 列表供 AI 上下文参考                                                                      |
| `public.sql`                    | 更新建表语句                                                                                                                                |

### 4.4 保持不变的表

| 表                               | 说明                 |
|----------------------------------|----------------------|
| `agent_chat_session`             | 智能体聊天会话       |
| `agent_chat_message`             | 智能体聊天消息       |
| `chat_turn`                      | 对话轮次             |
| `execution_event`                | 执行事件审计日志     |
| `task`                           | 任务实例（只改字段） |
| `task_detail`                    | 任务步骤实例         |
| `atomic_command`                 | 原子命令定义         |
| `agent_client`                   | 客户端实例           |
| `agent_executor`                 | 执行器类型           |
| `agent_definition`               | 智能体定义           |
| `agent_rule`                     | 智能体规则           |
| `agent_skill`                    | 智能体技能           |
| `sub_agent_relation`             | 子智能体关系         |
| `ai_model` / `ai_model_provider` | 模型/供应商          |
| `ai_user`                        | 用户                 |

---

## 五、关键文件索引

| 文件                          | 路径                                                 | 改动类型 | 说明                                                              |
|-------------------------------|------------------------------------------------------|----------|-------------------------------------------------------------------|
| AgentMemory                   | `common/entity/agentMemory/AgentMemory.java`         | 新增     | 新记忆实体，含 params_definition                                  |
| AgentMemoryStep               | `common/entity/agentMemoryStep/AgentMemoryStep.java` | 新增     | 新记忆步骤实体，含 args_template                                  |
| MemoryDistiller               | `common/service/memory/MemoryDistiller.java`         | 新增     | 蒸馏接口                                                          |
| DefaultMemoryDistiller        | `service/memory/DefaultMemoryDistiller.java`         | 新增     | 蒸馏实现：读取 task → AI 识别参数 → 创建 memory                   |
| MemoryExecutor                | `common/service/memory/MemoryExecutor.java`          | 新增     | 直执行接口                                                        |
| DefaultMemoryExecutor         | `service/memory/DefaultMemoryExecutor.java`          | 新增     | 直执行实现：memory → task → 占位符替换 → 下发                     |
| MemoryMatcher                 | `service/memory/MemoryMatcher.java`                  | 新增     | 记忆匹配器                                                        |
| Task                          | `common/entity/task/Task.java`                       | 修改     | 删除 agentMemoryId/memoryVersionId，新增 memoryId/memoryVersionNo |
| DefaultCommandDispatchService | `service/command/DefaultCommandDispatchService.java` | 修改     | 删除旧记忆逻辑，接入 MemoryDistiller                              |
| DefaultAgentChatService       | `service/agentChat/DefaultAgentChatService.java`     | 修改     | 删除旧蒸馏逻辑，接入 MemoryDistiller                              |
| AgentContextAssembler         | `service/agent/AgentContextAssembler.java`           | 修改     | 删除旧 AgentMemoryMatcher，加载新记忆列表                         |
| public.sql                    | `doc/sql/public.sql`                                 | 修改     | 删除旧表、新增新表、修改 task 表                                  |

---

## 六、风险与注意事项

| 风险                 | 缓解措施                                              |
|----------------------|-------------------------------------------------------|
| 旧代码删除遗漏       | 编译时会因为引用不存在而报错，逐项修复                |
| 新表字段与旧数据兼容 | 开发阶段无线上数据，直接重建表                        |
| AI 参数识别不准确    | 蒸馏结果存为 DRAFT，用户可在前端确认/修改后再 publish |
| 占位符替换边界情况   | 实现时覆盖空值、嵌套占位符、参数缺失等异常场景        |
| 记忆版本号并发       | 使用分布式锁保护同一 memory_id 的版本号递增           |

---

## 七、API 接口设计

### 7.1 接口总览

| 接口             | 方法 | 路径                                       | 说明                  |
|------------------|------|--------------------------------------------|-----------------------|
| 分页查询记忆     | POST | `/api/agent-memory/find-all`               | 管理后台列表          |
| 查询记忆详情     | POST | `/api/agent-memory/info`                   | 含步骤列表            |
| 获取执行参数定义 | GET  | `/api/agent-memory/{id}/params-definition` | 前端动态生成执行表单  |
| 执行记忆         | POST | `/api/agent-memory/{id}/execute`           | 创建任务 + 下发客户端 |
| 发布记忆         | POST | `/api/agent-memory/{id}/publish`           | DRAFT → PUBLISHED     |
| 淘汰记忆         | POST | `/api/agent-memory/{id}/retire`            | PUBLISHED → RETIRED   |
| 分页查询记忆步骤 | POST | `/api/agent-memory-step/find-all`          | 按 memory_id 查询     |
| 新增记忆步骤     | POST | `/api/agent-memory-step/create`            | 手动添加步骤          |
| 修改记忆步骤     | POST | `/api/agent-memory-step/update`            | 修改步骤参数          |
| 删除记忆步骤     | POST | `/api/agent-memory-step/delete`            | 删除步骤              |

### 7.2 核心接口详细设计

#### 7.2.1 分页查询记忆列表

```
POST /api/agent-memory/find-all

Request:
{
  "current": 1,
  "size": 10,
  "agentId": "xxx",         // 可选，按智能体筛选
  "versionStatus": 2,       // 可选，按版本状态筛选
  "keyword": "微信"          // 可选，按记忆名称模糊搜索
}

Response:
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": "xxx",
        "agentId": "xxx",
        "agentName": "微信助手",          // JOIN agent_definition 获取
        "memoryName": "向{contact_name}发送{message_content}",
        "versionNo": 1,
        "versionStatus": 2,              // 1-DRAFT / 2-PUBLISHED / 3-RETIRED
        "stepCount": 3,                  // 聚合：步骤数量
        "paramCount": 2,                 // 聚合：params_definition 中的参数数量
        "summary": "通过微信搜索指定联系人并发送文本消息",
        "createReason": "AI_EXPLORATION",
        "createTime": "2026-07-24 10:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

#### 7.2.2 获取执行参数定义

```
GET /api/agent-memory/{id}/params-definition

Response:
{
  "code": 200,
  "data": {
    "memoryId": "xxx",
    "memoryName": "向{contact_name}发送{message_content}",
    "paramsDefinition": {
      "contact_name": {
        "type": "string",
        "description": "收件人名称，如'文件传输助手'、'张三'",
        "required": true
      },
      "message_content": {
        "type": "string",
        "description": "要发送的消息内容",
        "required": true
      }
    },
    "steps": [
      {
        "sequenceNo": 10,
        "stepName": "打开微信",
        "atomicCommandCode": "start weixin",
        "argsTemplate": null
      },
      {
        "sequenceNo": 20,
        "stepName": "搜索联系人",
        "atomicCommandCode": "weixin_search_contact",
        "argsTemplate": {"contact_name": "{contact_name}"}
      },
      {
        "sequenceNo": 30,
        "stepName": "发送消息",
        "atomicCommandCode": "weixin_send_message",
        "argsTemplate": {"message_content": "{message_content}"}
      }
    ]
  }
}
```

#### 7.2.3 执行记忆

```
POST /api/agent-memory/{id}/execute

Request:
{
  "params": {
    "contact_name": "张三",
    "message_content": "在吗"
  },
  "clientId": "xxx"              // 可选，指定客户端实例；不传则使用记忆绑定的客户端
}

Response:
{
  "code": 200,
  "data": {
    "taskId": "xxx",
    "execStatus": "RUNNING",
    "memoryId": "xxx",
    "memoryVersionNo": 1,
    "taskDetails": [
      {"sequenceNo": 10, "stepName": "打开微信", "execStatus": "PENDING"},
      {"sequenceNo": 20, "stepName": "搜索联系人", "execStatus": "PENDING"},
      {"sequenceNo": 30, "stepName": "发送消息", "execStatus": "PENDING"}
    ]
  }
}
```

#### 7.2.4 发布记忆

```
POST /api/agent-memory/{id}/publish

Request:
{}

Response:
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": "xxx",
    "versionStatus": 2       // PUBLISHED
  }
}
```

### 7.3 参数校验规则

执行记忆时，后端对 `params` 的校验规则：

| 校验项       | 规则                                                                         |
|--------------|------------------------------------------------------------------------------|
| 必填参数     | 遍历 `params_definition`，`required=true` 的参数必须在 `params` 中存在且非空 |
| 类型匹配     | `type=string` 时校验值为字符串                                               |
| 多余参数     | 忽略 `params` 中不在 `params_definition` 中的键（不报错）                    |
| 无参数记忆   | `params_definition` 为空或 null 时，`params` 可为空                          |
| 占位符完整性 | 执行前检查所有 `args_template` 中的占位符是否都能在 `params` 中找到对应值    |

---

## 八、管理后台页面设计

### 8.1 记忆列表页

```
┌──────────────────────────────────────────────────────────────────┐
│ 记忆管理                                                          │
│                                                                    │
│ [新增记忆]  🔍 搜索记忆名称...                                      │
│                                                                    │
│ ┌────────────────┬────────┬──────┬──────┬──────┬──────────┬────┐ │
│ │ 记忆名称         │ 版本   │ 状态  │ 步骤数 │ 参数数 │ 创建时间  │ 操作│ │
│ ├────────────────┼────────┼──────┼──────┼──────┼──────────┼────┤ │
│ │ 向{contact_name}发送{message_content} │ v1 │ 已发布 │ 3 │ 2 │ 7/24 │ 查看 编辑 执行│ │
│ │ 打开{app_name}应用 │ v1 │ 草稿 │ 1 │ 1 │ 7/23 │ 查看 编辑 发布│ │
│ └────────────────┴────────┴──────┴──────┴──────┴──────────┴────┘ │
└──────────────────────────────────────────────────────────────────┘
```

**列表交互规则：**

- 状态列使用 `Tag` 组件：已发布=绿色、草稿=蓝色、已淘汰=灰色
- 操作列：草稿状态显示"查看/编辑/发布/删除"；已发布状态显示"查看/编辑/执行/淘汰"；已淘汰状态显示"查看"
- 记忆名称中的 `{param}` 占位符用高亮样式显示（如蓝色背景）
- 参数数列统计 `params_definition` JSON 中的 key 数量

### 8.2 记忆详情页

```
┌──────────────────────────────────────────────────────────────────┐
│ 记忆详情                                  [编辑] [发布] [执行]      │
│                                                                    │
│ ┌─ 基本信息 ────────────────────────────────────────────────────┐ │
│ │ 记忆名称：向{contact_name}发送{message_content}                │ │
│ │ 所属智能体：微信助手                                            │ │
│ │ 版本：v1  │  状态：已发布  │  创建原因：AI探索                  │ │
│ │ 摘要：通过微信搜索指定联系人并发送文本消息                       │ │
│ └────────────────────────────────────────────────────────────────┘ │
│                                                                    │
│ ┌─ 参数定义 ────────────────────────────────────────────────────┐ │
│ │ 参数名            │ 类型   │ 必填 │ 说明                      │ │
│ │ contact_name     │ string │ 是   │ 收件人名称，如"文件传输助手" │ │
│ │ message_content  │ string │ 是   │ 要发送的消息内容            │ │
│ └────────────────────────────────────────────────────────────────┘ │
│                                                                    │
│ ┌─ 步骤列表 ────────────────────────────────────────────────────┐ │
│ │ 序号 │ 步骤名称    │ 原子命令                    │ 参数模板               │ │
│ │ 10   │ 打开微信    │ start weixin               │ -                      │ │
│ │ 20   │ 搜索联系人  │ weixin_search_contact      │ contact_name: {contact_name} │ │
│ │ 30   │ 发送消息    │ weixin_send_message        │ message_content: {message_content} │ │
│ └────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### 8.3 执行记忆弹窗（核心交互）

用户点击"执行"后，根据 `params_definition` 动态生成表单。动态参数以集合形式呈现，`params_definition` 中的每个 key 渲染为一个表单项。

#### 有参数记忆

```
┌──────────────────────────────────────────┐
│  执行记忆                    ✕          │
│                                          │
│  记忆名称：向{contact_name}发送           │
│            {message_content}              │
│                                          │
│  ┌─ 执行参数 ──────────────────────────┐ │
│  │                                     │ │
│  │  收件人名称 *                        │ │
│  │  ┌──────────────────────────────┐   │ │
│  │  │ 张三                          │   │ │
│  │  └──────────────────────────────┘   │ │
│  │  如"文件传输助手"、"张三"            │ │
│  │                                     │ │
│  │  消息内容 *                          │ │
│  │  ┌──────────────────────────────┐   │ │
│  │  │ 在吗                          │   │ │
│  │  └──────────────────────────────┘   │ │
│  │                                     │ │
│  └─────────────────────────────────────┘ │
│                                          │
│  ┌─ 执行预览 ──────────────────────────┐ │
│  │ 步骤1: 打开微信                     │ │
│  │ 步骤2: 搜索联系人 → 张三            │ │
│  │ 步骤3: 发送消息 → 在吗              │ │
│  └─────────────────────────────────────┘ │
│                                          │
│            [取消]    [确认执行]           │
└──────────────────────────────────────────┘
```

#### 无参数记忆

```
┌──────────────────────────────────────┐
│  执行记忆                ✕          │
│                                      │
│  记忆名称：打开微信                   │
│                                      │
│  ┌─ 执行预览 ──────────────────────┐ │
│  │ 步骤1: 打开微信                 │ │
│  └─────────────────────────────────┘ │
│                                      │
│            [取消]    [确认执行]       │
└──────────────────────────────────────┘
```

### 8.4 前端实现要点

| 要点       | 说明                                                                                             |
|------------|--------------------------------------------------------------------------------------------------|
| 动态表单   | 调用 `GET /api/agent-memory/{id}/params-definition` 获取 `paramsDefinition`，遍历 key 生成表单项 |
| 表单类型   | 当前仅 `type=string`，使用 `Input` 组件；后续扩展 `type=number` 等                               |
| 执行预览   | 实时替换：将用户输入的值填入 `args_template` 的占位符位置，展示在预览区                          |
| 防重复提交 | 确认执行按钮使用 `usePreventDoubleClick` Hook，执行期间禁用                                      |
| 参数校验   | 前端先校验必填项非空，后端再校验 `params_definition` 完整性和类型                                |
| 执行结果   | 提交后跳转到任务详情页，通过 WebSocket 实时接收步骤执行进度                                      |

---

## 九、执行步骤清单

### 阶段一：数据库变更

- [x] 步骤1: 使用 db-ddl-generator 生成完整 SQL（删除旧表5张 + 新增新表2张 + 修改 task 表）
- [x] 步骤2: 更新 public.sql 文件

### 阶段二：删除旧代码

- [x] 步骤3: 删除旧 agent_memory 全套代码（Entity/DTO/Service/Controller/View/Copy/Repository/XML）
- [x] 步骤4: 删除旧 agent_memory_detail 全套代码
- [x] 步骤5: 删除旧 agent_memory_version 全套代码
- [x] 步骤6: 删除旧 agent_memory_version_detail 全套代码
- [x] 步骤7: 删除 memory_evidence 全套代码
- [x] 步骤8: 删除 AgentMemoryDistiller 接口 + DefaultAgentMemoryDistiller 实现
- [x] 步骤9: 删除 AgentMemoryMatcher（旧）
- [x] 步骤10: 删除 AgentMemorySummarizer

### 阶段三：修改 Task 实体

- [x] 步骤11: 修改 Task.java（删除 agentMemoryId/memoryVersionId，新增 memoryId/memoryVersionNo）
- [x] 步骤12: 修改 task 相关 DTO
- [x] 步骤13: 修改 task 相关 View/Repository/XML

### 阶段四：新增核心服务

- [x] 步骤14: 创建 AgentMemory 实体 + DTO + View + CopyMapper + Repository
- [x] 步骤15: 创建 AgentMemoryStep 实体 + DTO + View + CopyMapper + Repository
- [x] 步骤16: 创建 AgentMemoryController + AgentMemoryStepController
- [x] 步骤17: 创建 AgentMemoryService + DefaultAgentMemoryService
- [x] 步骤18: 创建 AgentMemoryStepService + DefaultAgentMemoryStepService
- [x] 步骤19: 创建 MemoryDistiller 接口 + DefaultMemoryDistiller 实现（含 AI 参数识别）
- [x] 步骤20: 创建 MemoryExecutor 接口 + DefaultMemoryExecutor 实现（含占位符替换）
- [x] 步骤21: 创建 MemoryMatcher（新匹配逻辑）

### 阶段五：修改现有服务

- [x] 步骤22: 修改 DefaultCommandDispatchService（接入 MemoryDistiller + MemoryMatcher）
- [x] 步骤23: 修改 DefaultAgentChatService（接入 MemoryDistiller）
- [x] 步骤24: 修改 AgentContextAssembler（加载新记忆列表）

### 阶段六：编译与自检

- [x] 步骤25: mvn clean package 编译验证
- [x] 步骤26: code-inspector 深度自检