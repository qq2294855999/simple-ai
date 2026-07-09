# 智能体原子命令调度与记忆闭环开发计划

## 需求一句话概括

基于重新设计后的智能体定义、规则、技能、子智能体关系、记忆、记忆详情、原子命令、任务和任务详情基础代码，实现统一命令下发核心：HTTP 与 WebSocket 只作为入口适配器，核心服务负责装配“系统铁律 + 智能体定义 + 直属规则 + 直属技能 + 会话摘要 + 候选记忆”，创建任务与任务详情，安全执行原子命令或记录待人工处理，并在成功后沉淀可复用记忆。

## 技术栈

- 后端：Spring Boot。
- AI 能力：固定采用 Spring AI，不接入 simple-common-ai。
- 数据访问：MyBatis-Plus，沿用当前 View / Repository / Mapper XML 分层。
- Redis 会话：使用当前 Redis 配置保存会话历史与摘要；simple-common-redis 优先用于锁、限流、并发控制。
- 实时入口：WebSocket，按 simple-common-websocket 的 `@WebSocketListening` 监听分发方式做入口适配。

## 最新设计图与 SQL 对齐结论

从设计图、[`doc/sql/agent-design-full-postgresql.sql`](../sql/agent-design-full-postgresql.sql)和当前重新生成后的基础代码可确认，核心实体关系如下：

- 智能体定义：保存智能体名称、定义描述、第一铁律、第二规则、第三技能、模型、状态、扩展、备注。
- 智能体规则：通过自身 `agent_id` 直接归属智能体，不再需要智能体规则关联表。
- 智能体技能：通过自身 `agent_id` 直接归属智能体，不再需要智能体技能关联表。
- 子智能体关联：通过主智能体 ID 和子智能体 ID 表达协作关系。
- 智能体记忆：保存某个智能体在某类任务上的可复用经验。
- 智能体记忆详情：保存记忆对应的步骤链路、判断分支与路由。
- 原子命令：保存可被记忆详情或任务详情引用的标准命令定义。
- 任务：一次用户下发请求形成的运行实例，记录执行状态与失败原因。
- 任务详情：任务执行过程中的每个步骤、请求、响应与状态记录。

## 已作废的旧设计

| 作废项 | 作废原因 | 当前处理 |
|---|---|---|
| AgentDefinitionRuleAssociation 智能体规则关联表 | 最新表结构中 `agent_rule.agent_id` 已直接绑定智能体 | 不创建、不扩展、不引用 |
| AgentDefinitionSkillAssociation 智能体技能关联表 | 最新表结构中 `agent_skill.agent_id` 已直接绑定智能体 | 不创建、不扩展、不引用 |
| 删除 AgentDefinition 的 firstPrinciple / secondRule / thirdSkill | 最新 SQL 和设计图仍保留这些字段 | 保留字段，并参与上下文装配 |
| 为废弃字段和关联表输出数据库变更 SQL | 最新全量 SQL 已是设计基准 | 如需 SQL，仅输出索引、初始化样例或运行辅助脚本 |

## 内容存储格式设计

### 设计结论

规则、技能、记忆与命令正文建议继续采用“Markdown + YAML Front Matter”的文本格式落库；系统级 Agent 铁律固定在 Java 常量类中，智能体定义中的第一铁律、第二规则、第三技能也作为智能体自身提示词片段参与上下文装配。

### 选择理由

- Markdown 适合直接拼接为大模型系统提示词与开发者提示词，可读性强。
- YAML Front Matter 适合放置可机器解析的元信息，例如版本、类型、优先级、适用智能体、触发条件、依赖原子命令。
- 数据库仍保存字符串，不强制复杂 JSON 字段，能兼容当前已生成实体的 String 字段。
- 系统级 Agent 铁律必须稳定置顶，不允许被配置误删或降级，因此写死到 `AgentIronRuleConstant`。
- 智能体定义中的第一铁律、第二规则、第三技能是业务设计图的一部分，不删除、不废弃。

### 存储样例

```md
---
type: RULE
priority: 80
version: 1
agentId: agent-001
status: ENABLED
---
# 规则说明

这里保存规则正文。
```

## 业务流程图

```text
用户或第三方系统下发标准命令请求
  ↓
□ 入口适配层接收请求
  ├── HTTP → □ CommandDispatchController
  └── WebSocket → □ CommandWebSocketEndpoint(@WebSocketListening)
  ↓
□ 转换为统一 CommandDispatchRequest
  ↓
□ CommandDispatchService 接管核心业务
  ↓
◇ 智能体是否存在且启用？
  ├── 否 → □ 创建失败 Task 并写入 failureReason → [任务失败原因]
  └── 是
      ↓
□ 读取智能体定义 AgentDefinition
  ↓
□ 读取直属规则 AgentRule(agentId)
  ↓
□ 读取直属技能 AgentSkill(agentId)
  ↓
□ 读取 Redis 会话摘要
  ↓
□ 匹配候选记忆 AgentMemory(agentId + triggerCondition)
  ↓
□ 按优先级组装上下文
  ↓
[系统铁律] → [智能体定义铁律/规则/技能] → [直属规则] → [直属技能] → [会话摘要] → [候选记忆]
  ↓
◇ 是否命中可复用记忆？
  ├── 是 → □ 读取记忆详情 → □ 写入任务详情 → □ 安全执行原子命令
  │           ↓
  │       ◇ 执行是否成功？
  │           ├── 是 → □ 写入任务成功详情 → [任务成功]
  │           └── 否 → □ 记录失败步骤 → □ 写入 failureReason → [任务失败]
  └── 否 → □ Spring AI 根据上下文生成下一步建议
              ↓
          □ 写入探索任务详情
              ↓
          □ 安全默认执行器返回标准结果或待人工处理
              ↓
          ◇ 是否达到用户目标？
              ├── 是 → □ 提炼最短命令路径 → □ 保存/更新记忆 → [任务成功]
              └── 否 → □ 写入 failureReason → [任务失败]
  ↓
□ 更新 Redis 会话摘要
  ↓
□ 返回统一 CommandDispatchResponse
```

## 核心架构设计

### 入口与核心解耦

采用“适配器模式 + 门面服务”设计。

- HTTP 入口只负责协议层参数接收、鉴权、校验、响应包装。
- WebSocket 入口只负责消息收发、客户端通道上下文处理。
- 两种入口都调用同一个 `CommandDispatchService`。
- `CommandDispatchService` 是唯一任务下发核心，避免协议差异污染业务逻辑。

### AI 执行编排

采用“策略模式 + 执行器注册表”设计。

- `AtomicCommandExecutor` 定义原子命令执行标准接口。
- 不同命令类型后续可以实现不同执行器，例如读取信息、写入信息、调用工具、调用子智能体。
- `AtomicCommandExecutorRegistry` 根据命令内容或命令类型选择执行器。
- AI 不直接操作具体执行器，只输出标准原子命令调用请求。
- `AgentAiClient` 只保留抽象边界，首期唯一实现为 `SpringAiAgentAiClient`。
- 首期默认执行器不得执行高风险系统命令，仅返回标准执行结果或记录待人工处理。

### 记忆复用闭环

采用“模板方法”组织任务执行生命周期。

- 创建任务。
- 装载智能体上下文。
- 匹配记忆。
- 执行记忆命令或探索命令。
- 记录任务详情。
- 成功后提炼并保存记忆。
- 失败后记录失败原因到 `Task.failureReason`。
- 更新 Redis 会话。

## 源码自检结论

### 已验证项目依赖

| 文件 | 结论 |
|------|------|
| [`pom.xml`](../../pom.xml) | 当前已有 simple-common-oauth-start、simple-common-mp、simple-common-eventbus、simple-common-redis、simple-common-websocket、Spring AI OpenAI starter、spring-boot-starter、spring-boot-starter-test。 |
| [`src/main/resources/application-local.yaml`](../../src/main/resources/application-local.yaml) | 已存在 Redis 连接配置、Spring AI OpenAI 配置、simple.websocket.command 配置。 |
| [`src/main/java/com/simple/ai/AIApplication.java`](../../src/main/java/com/simple/ai/AIApplication.java) | 标准 Spring Boot 启动类。 |

### 已验证基础实体

| 实体 | 路径 | 结论 |
|------|------|------|
| AgentDefinition | [`src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java) | 已存在，字段包含 name、definitionDesc、firstPrinciple、secondRule、thirdSkill、status、model、reserver、remark。 |
| AgentRule | [`src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java`](../../src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java) | 已存在，字段包含 agentId、definitionDesc、triggerCondition、triggerAction、status、reserver、remark。 |
| AgentSkill | [`src/main/java/com/simple/ai/common/entity/agentSkill/AgentSkill.java`](../../src/main/java/com/simple/ai/common/entity/agentSkill/AgentSkill.java) | 已存在，字段包含 agentId、definitionDesc、execContent、returnDataFormat、status、reserver、remark。 |
| SubAgentRelation | [`src/main/java/com/simple/ai/common/entity/subAgentRelation/SubAgentRelation.java`](../../src/main/java/com/simple/ai/common/entity/subAgentRelation/SubAgentRelation.java) | 已存在，字段包含 mainAgentId、subAgentId。 |
| AgentMemory | [`src/main/java/com/simple/ai/common/entity/agentMemory/AgentMemory.java`](../../src/main/java/com/simple/ai/common/entity/agentMemory/AgentMemory.java) | 已存在，字段包含 agentId、memoryName、stepName、triggerCondition、triggerAction、status。 |
| AgentMemoryDetail | [`src/main/java/com/simple/ai/common/entity/agentMemoryDetail/AgentMemoryDetail.java`](../../src/main/java/com/simple/ai/common/entity/agentMemoryDetail/AgentMemoryDetail.java) | 已存在，字段包含 agentMemoryId、stepName、stepType、execContent、returnDataFormat、parentStepId、nextStepId、branchCondition、branchRoute、model。 |
| AtomicCommand | [`src/main/java/com/simple/ai/common/entity/atomicCommand/AtomicCommand.java`](../../src/main/java/com/simple/ai/common/entity/atomicCommand/AtomicCommand.java) | 已存在，字段包含 name、command、role、status、reserver、remark。 |
| Task | [`src/main/java/com/simple/ai/common/entity/task/Task.java`](../../src/main/java/com/simple/ai/common/entity/task/Task.java) | 已存在，字段包含 agentMemoryId、taskName、parentTaskId、nextTaskId、stepType、branchCondition、branchRoute、requestParams、returnParams、execStatus、failureReason。 |
| TaskDetail | [`src/main/java/com/simple/ai/common/entity/taskDetail/TaskDetail.java`](../../src/main/java/com/simple/ai/common/entity/taskDetail/TaskDetail.java) | 已存在，字段包含 taskId、taskName、parentTaskId、nextTaskId、stepType、branchCondition、branchRoute、requestParams、returnParams、execStatus。 |

### 已验证现有 CRUD 链路

| 模块 | Controller | Service | View | Repository | Mapper XML | 结论 |
|------|------------|---------|------|------------|------------|------|
| 智能体规则 | 已验证 | 已验证 | 已验证 | 已验证 | 已验证 | 当前分页和精确查询均支持 agentId，可用于上下文装配。 |
| 智能体定义 | 已验证实体和 Mapper | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 已验证 | 当前保留定义内铁律、规则、技能字段。 |
| 智能体技能 | 已验证实体 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 当前通过 agentId 直接归属智能体。 |
| 智能体记忆 | 已验证实体 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 后续用于候选记忆匹配。 |
| 智能体记忆详情 | 已验证实体 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 后续用于记忆命令链路读取。 |
| 原子命令 | 已验证实体 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 后续用于命令执行器输入。 |
| 任务 | 已验证实体和 Mapper | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 已验证 | 已包含 failureReason 字段。 |
| 任务详情 | 已验证实体 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 待实现阶段按需通读 | 用于记录执行步骤。 |

## 需要新增的核心模块

| 模块 | 类型 | 说明 |
|------|------|------|
| command DTO | 新增 | 统一 HTTP 与 WebSocket 的任务下发请求、响应、原子命令调用参数、任务执行事件。 |
| agent DTO | 新增 | 封装 AgentContext、AgentAiRequest、AgentAiResponse。 |
| CommandDispatchController | 新增 | HTTP 单接口入口。 |
| CommandWebSocketEndpoint | 新增 | 使用 `@WebSocketListening` 的 WebSocket 同格式入口。 |
| CommandDispatchService | 新增 | 核心任务下发门面，所有入口必须调用它。 |
| DefaultCommandDispatchService | 新增 | 编排任务创建、上下文装配、记忆匹配、命令执行、任务详情记录。 |
| AgentIronRuleConstant | 新增 | 保存系统级强制置顶的智能体铁律。 |
| AgentContextAssembler | 新增 | 按“系统铁律 > 智能体定义 > 直属规则 > 直属技能 > 会话 > 记忆”组装上下文。 |
| AgentMemoryMatcher | 新增 | 根据智能体 ID、任务内容、triggerCondition 筛选候选记忆。 |
| AgentMemorySummarizer | 新增 | 成功探索后提炼最短命令路径并保存记忆。 |
| AgentSessionService | 新增 | Redis 会话保存、读取、摘要追加。 |
| AtomicCommandExecutor | 新增 | 原子命令执行器接口。 |
| AtomicCommandExecutorRegistry | 新增 | 命令执行器注册表。 |
| DefaultAtomicCommandExecutor | 新增 | 默认安全原子命令执行逻辑。 |
| AgentAiClient | 新增 | AI 调用抽象，仅隔离 Spring AI 实现。 |
| SpringAiAgentAiClient | 新增 | 固定使用 Spring AI 进行对话调用。 |
| AgentExecutionStatusProcess | 新增 | 任务状态枚举，统一等待、执行中、成功、失败。 |
| AgentStepTypeProcess | 新增 | 智能体步骤类型枚举，统一判断、原子命令、循环开始、循环结束。 |

## 现有实体需要确认的点

| 对象 | 当前情况 | 调整结论 |
|------|----------|----------|
| AgentDefinition.firstPrinciple / secondRule / thirdSkill | 当前为 String | 保留字段，参与上下文装配。 |
| AgentRule.agentId | 当前已存在 | 作为规则直属智能体的绑定字段。 |
| AgentSkill.agentId | 当前已存在 | 作为技能直属智能体的绑定字段。 |
| Agent 铁律 | 当前无独立实体 | 新增系统级 AgentIronRuleConstant，每次装配智能体上下文时强制置顶。 |
| Task.agentMemoryId | 当前任务可关联一个记忆 | 新任务不一定命中记忆，允许为空；探索成功后可回填命中的或新建的记忆 ID。 |
| Task.execStatus | 当前为 String | 新增枚举常量统一状态值，实体字段保持 String。 |
| Task.failureReason | 当前已存在 | 失败链路必须写入。 |
| TaskDetail.stepType | 当前为 String | 新增枚举常量统一步骤类型。 |
| AtomicCommand.command | 当前为 String | 保存标准命令模板，采用 Markdown + YAML Front Matter 或纯 YAML 命令定义。 |
| AtomicCommand.role | 当前为 String | 用于说明命令作用，后续可辅助执行器选择。 |

## 依赖设计

### simple-common 优先结论

已读取以下 simple-common 技能文档：

| 模块 | 用途 | 采用建议 |
|------|------|----------|
| simple-common-redis | Redis 缓存、限流、锁能力 | 会话历史和摘要可使用 RedisTemplate 保持结构可控；并发控制、任务锁、限流优先使用 simple-common-redis 能力。 |
| simple-common-websocket | WebSocket 消息分发能力 | WebSocket 入口使用该模块提供的 `@WebSocketListening`。 |

### 技术验证结论

- AI 实现固定为 Spring AI，不引入 simple-common-ai，不保留 simple-common-ai 兜底方案。
- WebSocket 入口按 simple-common-websocket 的监听分发方式实现。
- Redis 会话保存使用 Redis，保持会话结构可控；simple-common-redis 优先用于锁、限流、并发控制。
- Task 已具备 failureReason 字段。
- AgentRule 与 AgentSkill 通过 agentId 直接归属智能体。
- AgentDefinition 保留第一铁律、第二规则、第三技能字段。
- 不新增智能体规则关联表和智能体技能关联表。

## 开发计划

- 步骤一：修订执行进度文档，建立最新恢复入口与逐步标记清单。
- 步骤二：修订本总计划文档，移除过期关联表设计，改为 agentId 直属规则/技能查询。
- 步骤三：进行计划技术验证，确认依赖兼容性、关键类存在性、逻辑自洽性。
- 步骤四：新增 command 统一 DTO：CommandDispatchRequest、CommandDispatchResponse、AtomicCommandInvokeRequest、AtomicCommandInvokeResponse、CommandDispatchEvent。
- 步骤五：新增 agent 过程 DTO：AgentContext、AgentAiRequest、AgentAiResponse。
- 步骤六：新增 AgentExecutionStatusProcess 枚举，统一任务状态为等待执行、执行中、执行成功、执行失败。
- 步骤七：新增 AgentStepTypeProcess 枚举，统一步骤类型为判断、原子命令、循环开始、循环结束。
- 步骤八：新增 AgentIronRuleConstant 常量类，保存系统级强制置顶的智能体铁律文本。
- 步骤九：新增 AgentAiClient 接口，隔离模型调用实现。
- 步骤十：新增 SpringAiAgentAiClient，实现 Spring AI 对话调用。
- 步骤十一：新增 AgentSessionService 接口与 RedisAgentSessionService 实现，用 Redis 保存会话历史与任务摘要。
- 步骤十二：新增 AgentContextAssembler，读取 AgentDefinition、AgentRule、AgentSkill、AgentMemory、AgentMemoryDetail 并按优先级拼装上下文。
- 步骤十三：新增 AgentMemoryMatcher，基于智能体 ID、任务内容、triggerCondition 筛选候选记忆。
- 步骤十四：新增 AtomicCommandExecutor 接口，定义 supports 与 execute 方法。
- 步骤十五：新增 AtomicCommandExecutorRegistry，管理所有原子命令执行器。
- 步骤十六：新增 DefaultAtomicCommandExecutor，实现默认安全执行与标准结果返回。
- 步骤十七：新增 AgentMemorySummarizer，探索成功后将任务详情提炼为最短可复用命令链路。
- 步骤十八：新增 CommandDispatchService 接口，定义 dispatch 作为唯一核心入口。
- 步骤十九：新增 DefaultCommandDispatchService，编排任务生命周期、记忆命中、探索执行、任务详情记录、失败记录、会话保存。
- 步骤二十：新增 CommandDispatchController，提供 HTTP 单接口，例如 POST sys/agent-command/dispatch。
- 步骤二十一：新增 CommandWebSocketEndpoint，提供 WebSocket 同数据格式入口。
- 步骤二十二：按需新增 doc/sql/agent-command-dispatch-postgresql.sql，仅输出索引、初始化样例或运行辅助 SQL。
- 步骤二十三：每完成一个步骤立即更新执行进度文档。
- 步骤二十四：执行 mvn clean compile，确保编译通过。
- 步骤二十五：执行深度自检，重点检查入口不重复、核心内聚、任务闭环、记忆闭环、异常链路闭环。

## 关键文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|------|------|----------|------|
| 执行进度文档 | [`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) | 修改 | 作为会话中断后的恢复入口和逐步标记清单。 |
| 总计划文档 | [`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md) | 修改 | 按新设计图和新 SQL 重新梳理逻辑。 |
| AgentIronRuleConstant | [`src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java`](../../src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java) | 新增 | 固定保存系统级强制置顶的智能体铁律。 |
| CommandDispatchRequest | [`src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java) | 新增 | HTTP 与 WebSocket 共用任务下发请求。 |
| CommandDispatchResponse | [`src/main/java/com/simple/ai/common/dto/command/CommandDispatchResponse.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchResponse.java) | 新增 | HTTP 与 WebSocket 共用任务下发响应。 |
| AtomicCommandInvokeRequest | [`src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeRequest.java`](../../src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeRequest.java) | 新增 | 原子命令调用请求。 |
| AtomicCommandInvokeResponse | [`src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeResponse.java`](../../src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeResponse.java) | 新增 | 原子命令调用响应。 |
| AgentContext | [`src/main/java/com/simple/ai/common/dto/agent/AgentContext.java`](../../src/main/java/com/simple/ai/common/dto/agent/AgentContext.java) | 新增 | 智能体上下文对象。 |
| CommandDispatchService | [`src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java) | 新增 | 统一命令下发核心接口。 |
| DefaultCommandDispatchService | [`src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java) | 新增 | 统一任务调度核心实现。 |
| CommandDispatchController | [`src/main/java/com/simple/ai/controller/command/CommandDispatchController.java`](../../src/main/java/com/simple/ai/controller/command/CommandDispatchController.java) | 新增 | HTTP 单接口入口。 |
| CommandWebSocketEndpoint | [`src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java`](../../src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java) | 新增 | WebSocket 同格式入口。 |
| AgentContextAssembler | [`src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java`](../../src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java) | 新增 | 组装铁律、智能体定义、规则、技能、会话、记忆上下文。 |
| AgentMemoryMatcher | [`src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java`](../../src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java) | 新增 | 命中候选记忆。 |
| AgentMemorySummarizer | [`src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java`](../../src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java) | 新增 | 保存最短命令路径为记忆。 |
| AgentSessionService | [`src/main/java/com/simple/ai/common/service/session/AgentSessionService.java`](../../src/main/java/com/simple/ai/common/service/session/AgentSessionService.java) | 新增 | Redis 会话服务接口。 |
| RedisAgentSessionService | [`src/main/java/com/simple/ai/service/session/RedisAgentSessionService.java`](../../src/main/java/com/simple/ai/service/session/RedisAgentSessionService.java) | 新增 | Redis 会话服务实现。 |
| AtomicCommandExecutor | [`src/main/java/com/simple/ai/common/service/command/AtomicCommandExecutor.java`](../../src/main/java/com/simple/ai/common/service/command/AtomicCommandExecutor.java) | 新增 | 原子命令执行器接口。 |
| AtomicCommandExecutorRegistry | [`src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java`](../../src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java) | 新增 | 命令执行器注册表。 |
| AgentAiClient | [`src/main/java/com/simple/ai/common/service/agent/AgentAiClient.java`](../../src/main/java/com/simple/ai/common/service/agent/AgentAiClient.java) | 新增 | AI 调用抽象。 |
| SpringAiAgentAiClient | [`src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java`](../../src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java) | 新增 | Spring AI 调用实现。 |
| 辅助 SQL | [`doc/sql/agent-command-dispatch-postgresql.sql`](../sql/agent-command-dispatch-postgresql.sql) | 按需新增 | 仅包含索引、初始化样例或运行辅助 SQL。 |

## 阶段二技术验证清单

- AI 实现采用 Spring AI 实现。
- WebSocket 使用 simple-common-websocket 的 `@WebSocketListening`。
- Redis 会话保存使用 Redis，simple-common-redis 用于锁、限流和并发控制优先。
- Task 已包含 failureReason 字段。
- AgentRule 通过 agentId 直属智能体。
- AgentSkill 通过 agentId 直属智能体。
- AgentDefinition 保留 firstPrinciple、secondRule、thirdSkill，并参与上下文装配。
- 不创建 AgentDefinitionRuleAssociation。
- 不创建 AgentDefinitionSkillAssociation。

## 完成度复核与欠缺项补充

### 当前完成度结论

基于最新设计图、全量 SQL、现有代码和编译结果，当前项目已完成“基础数据模型 + 基础 CRUD + HTTP / WebSocket 统一入口 + 最小任务闭环”，并已补充部分整改项，例如子智能体关系进入上下文、候选记忆详情批量查询、自动记忆沉淀入口、Redis 会话消息窗口裁剪。

但项目尚未完成“按记忆详情步骤链严格执行、真实原子命令路由、子智能体协作执行、流式过程反馈、启用状态全链路过滤、批量沉淀写入、完整性能治理”的智能体运行闭环。

综合完成度调整为 **78%**。该结论以后续整改完成后重新评估。

### 欠缺项清单

| 欠缺项 | 当前表现 | 影响 | 整改方向 |
|---|---|---|---|
| 调度命令非流式 | HTTP dispatch 只在执行完成后返回最终结果 | 长任务期间调用方不知道 AI 正在做什么，也无法展示每一步进度 | 新增流式调度接口，按任务创建、上下文装配、记忆匹配、步骤执行、AI 响应、任务完成持续推送事件 |
| 流式事件模型缺失 | 当前只有 CommandDispatchResponse 最终响应 | 前端和 WebSocket 无法统一消费执行过程 | 新增 CommandDispatchProgressEvent，定义 eventType、taskId、stepName、message、payload、completed 等字段 |
| AI 调用非流式 | SpringAiAgentAiClient 只使用同步 chat 调用 | 模型生成较慢时无法逐段返回内容 | 在接口层先输出阶段性进度事件，后续按 Spring AI stream 能力扩展 token 级输出 |
| 记忆详情步骤链执行不完整 | 命中记忆后按列表顺序执行，没有按 parentStepId / nextStepId / branchRoute 路由 | 设计图中的判断、原子命令、循环开始、循环结束没有完整生效 | 新增步骤链游标执行器，按 nextStepId 推进，判断步骤按 branchRoute 选择下一步，循环步骤设置安全上限 |
| 原子命令真实执行器不足 | 当前默认执行器总是返回待人工处理 | atomic_command 表参与了匹配，但没有形成真实能力执行体系 | 保留默认安全执行器，同时为后续按 role / command type 扩展专用执行器预留注册规则 |
| 记忆匹配状态过滤缺失 | AgentMemoryMatcher 查询记忆时未限定 ENABLED | 禁用记忆可能参与执行链路 | 查询记忆时统一设置 Status.ON |
| 记忆详情状态过滤缺失 | 批量读取 memoryDetail 未限定 ENABLED | 禁用步骤可能被写入上下文并执行 | 批量查询详情时增加状态过滤或结果过滤 |
| 自动记忆沉淀仍逐条写入详情 | AgentMemorySummarizer 循环调用 agentMemoryDetailService.save | 任务详情较多时存在 N+1 写入风险 | 新增批量保存路径，或通过 View.saves 批量写入并保留必要校验 |
| 子智能体协作只进入上下文 | SubAgentRelation 只被写入 prompt，没有实际路由子智能体执行 | 设计图中的子智能体关系未成为执行能力 | 后续按子智能体命令类型创建子任务并递归调用调度核心 |
| 任务失败详情可能重复 | 外层 catch 统一保存失败 TaskDetail，部分步骤失败前已保存失败详情 | 失败链路可能出现重复记录 | 区分已记录失败详情与未记录异常，避免重复写入 |
| 步骤链循环缺少安全上限 | 当前未真正执行循环，也没有循环次数保护 | 后续实现循环后存在死循环风险 | 设置最大步骤数和最大循环次数，超限标记任务失败 |

### 新增流式调度业务流程图

```text
用户或第三方系统发起流式调度
  ↓
□ HTTP SSE / WebSocket 入口接收 CommandDispatchRequest
  ↓
□ 创建任务并立即推送 TASK_CREATED
  ↓
□ 装配上下文并推送 CONTEXT_ASSEMBLED
  ↓
□ 匹配候选记忆并推送 MEMORY_MATCHED / MEMORY_MISSED
  ↓
◇ 是否命中可用记忆？
  ├── 是
  │   ↓
  │ □ 按步骤链游标执行 AgentMemoryDetail
  │   ↓
  │ □ 每个步骤开始推送 STEP_STARTED
  │   ↓
  │ □ 每个步骤结束推送 STEP_COMPLETED / STEP_FAILED
  └── 否
      ↓
    □ 调用 Spring AI 探索
      ↓
    □ 推送 AI_STARTED / AI_COMPLETED
      ↓
    □ 成功后沉淀记忆并推送 MEMORY_SUMMARIZED
  ↓
◇ 任务是否成功？
  ├── 是 → □ 推送 TASK_COMPLETED
  └── 否 → □ 推送 TASK_FAILED
```

### 流式整改开发计划

- 步骤一：新增 CommandDispatchProgressEvent DTO，作为 HTTP SSE 与 WebSocket 共用的进度事件对象。
- 步骤二：扩展 CommandDispatchService，新增 dispatchStream 方法，返回 Java Stream 或面向 emitter 的事件发布能力。
- 步骤三：新增 CommandDispatchProgressPublisher 接口，统一进度事件发布边界，避免核心服务依赖具体协议。
- 步骤四：新增 HTTP 流式接口，例如 POST sys/agent-command/dispatch-stream，优先采用 SseEmitter 输出进度事件。
- 步骤五：修改 WebSocket 入口，调度过程中逐条写回 CommandDispatchProgressEvent，最终再写 TASK_COMPLETED / TASK_FAILED。
- 步骤六：重构 DefaultCommandDispatchService，将任务执行拆为可发布进度的阶段方法。
- 步骤七：修改 AgentMemoryMatcher，查询记忆统一限定启用状态。
- 步骤八：修改 AgentContextAssembler，候选记忆详情仅加载启用步骤。
- 步骤九：新增记忆详情步骤链游标执行逻辑，按 nextStepId、branchRoute、循环上限推进。
- 步骤十：修改 AgentMemorySummarizer，记忆详情沉淀改为批量写入，消除 N+1 写入。
- 步骤十一：执行 mvn clean compile，确保编译通过。
- 步骤十二：按 code-inspector 执行深度自检，发现问题后递归修复。

### 流式整改关键文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| CommandDispatchProgressEvent | [`src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java) | 新增 | 流式进度事件 DTO |
| CommandDispatchService | [`src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java) | 修改 | 增加流式调度入口 |
| CommandDispatchController | [`src/main/java/com/simple/ai/controller/command/CommandDispatchController.java`](../../src/main/java/com/simple/ai/controller/command/CommandDispatchController.java) | 修改 | 新增 HTTP SSE 流式接口 |
| CommandWebSocketEndpoint | [`src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java`](../../src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java) | 修改 | WebSocket 推送每一步事件 |
| DefaultCommandDispatchService | [`src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java) | 修改 | 核心执行过程发布进度，完善步骤链执行 |
| AgentMemoryMatcher | [`src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java`](../../src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java) | 修改 | 增加启用状态过滤 |
| AgentContextAssembler | [`src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java`](../../src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java) | 修改 | 过滤启用记忆详情 |
| AgentMemorySummarizer | [`src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java`](../../src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java) | 修改 | 批量沉淀记忆详情 |

## 开发边界

首期实现“命令下发核心闭环 + 流式过程反馈”的后端能力，不做前端对话窗口页面；对话窗口只需要调用 HTTP 流式接口或 WebSocket 统一消息格式即可。默认原子命令执行器不执行高风险系统命令，只输出标准执行结果或待人工处理记录。
