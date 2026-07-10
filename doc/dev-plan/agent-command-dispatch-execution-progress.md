# 智能体命令调度开发执行进度文档

## 文档目的

本文件是智能体命令调度、任务执行、记忆复用与记忆沉淀开发的唯一可恢复执行入口。后续每完成一个开发步骤，必须立即更新本文件中的状态、产物、验证结论与下一步入口，避免会话中断后上下文丢失。

## 状态约定

- `[x]` 已完成：代码或文档已落地，并经过当前阶段必要确认。
- `[-]` 进行中：当前正在处理，尚未完成闭环。
- `[ ]` 待处理：尚未开始。
- `[!]` 阻塞：需要用户或外部条件补充后才能继续。

## 当前总体状态

- 当前阶段：阶段五，Spring AI token 级流式输出已实现并完成深度自检。
- 当前依据：最新设计图、[`doc/sql/agent-design-full-postgresql.sql`](../sql/agent-design-full-postgresql.sql)、当前重新生成后的基础代码、总计划文档[`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md)。
- 当前结论：旧计划中“智能体规则关联表”和“智能体技能关联表”方向已失效；最新表结构中[`agent_rule`](../sql/agent-design-full-postgresql.sql:73)与[`agent_skill`](../sql/agent-design-full-postgresql.sql:45)均直接包含[`agent_id`](../sql/agent-design-full-postgresql.sql:48)，后续不得创建或扩展智能体规则/技能关联表。
- 当前完成度复核：基于当前代码、设计文档和设计图，整体完成度调整为 90%；已完成 HTTP SSE 流式调度、WebSocket 阶段级事件、记忆步骤链游标、启用状态过滤、批量记忆详情沉淀、专用原子命令执行器骨架、子智能体递归调度、判断循环真实语义、失败详情去重、候选记忆详情查询层状态过滤和 Spring AI token 级流式输出。
- 当前主要欠缺：暂无本轮已确认的编码缺口，后续按真实运行结果继续扩展白名单写入或工具能力。
- 当前执行要求：每完成一个步骤都必须先更新本文档，再进入下一步骤。

## 需求重新理解

基于最新设计图和当前基础代码，智能体运行逻辑应围绕“智能体定义 → 规则/技能/子智能体/记忆/原子命令 → 任务/任务详情”展开：

- 智能体定义保存智能体基础边界：名称、定义描述、第一铁律、第二规则、第三技能、模型、状态、扩展、备注。
- 智能体规则与智能体技能通过自身[`agentId`](../../src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java:47)直接归属智能体，不再需要中间关联表。
- 智能体记忆保存可复用经验，智能体记忆详情保存记忆步骤链路。
- 原子命令是可被任务详情或记忆详情引用的命令定义；当步骤需要执行原子命令时，根据步骤内容匹配命令。
- 任务是一次执行实例，任务详情记录每个执行步骤、分支、请求参数、返回参数和执行状态。
- 首期实现目标是先建立“统一命令下发入口 + 任务落库 + 上下文装配 + 记忆候选匹配 + 安全默认原子命令执行 + 任务详情记录 + 会话摘要保存”的后端闭环。

## 业务流程图

```text
用户或外部系统下发命令
  ↓
□ HTTP / WebSocket 入口接收请求
  ↓
□ 转换为统一 CommandDispatchRequest
  ↓
□ 查询智能体定义 AgentDefinition
  ↓
◇ 智能体是否存在且启用？
  ├── 否 → □ 创建失败 Task → □ 写入 failureReason → [CommandDispatchResponse]
  └── 是
      ↓
□ 读取智能体直属规则 AgentRule(agentId)
  ↓
□ 读取智能体直属技能 AgentSkill(agentId)
  ↓
□ 读取 Redis 会话摘要
  ↓
□ 匹配智能体记忆 AgentMemory(agentId + triggerCondition)
  ↓
□ 组装 AgentContext
  ↓
◇ 是否命中可用记忆？
  ├── 是 → □ 读取 AgentMemoryDetail 步骤链 → □ 写入 TaskDetail → □ 执行安全默认原子命令
  │           ↓
  │       ◇ 执行是否成功？
  │           ├── 是 → □ 更新 Task 成功 → □ 保存会话摘要 → [CommandDispatchResponse]
  │           └── 否 → □ 记录失败 TaskDetail → □ 写入 failureReason → [CommandDispatchResponse]
  └── 否 → □ 调用 Spring AI 生成下一步建议
              ↓
          □ 写入探索 TaskDetail
              ↓
          □ 安全默认原子命令执行或标记人工处理
              ↓
          ◇ 是否达到目标？
              ├── 是 → □ 可选沉淀 AgentMemory / AgentMemoryDetail → □ 更新 Task 成功
              └── 否 → □ 写入 failureReason → □ 更新 Task 失败
  ↓
□ 更新 Redis 会话摘要
  ↓
□ 返回统一响应
```

## 设计图与当前代码对齐结论

| 设计对象 | SQL 依据 | 当前代码依据 | 当前结论 |
|---|---|---|---|
| 智能体定义 | [`agent_definition`](../sql/agent-design-full-postgresql.sql:10) | [`AgentDefinition`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java:36) | 已包含第一铁律、第二规则、第三技能、模型等字段，应保留并纳入上下文。 |
| 智能体技能 | [`agent_skill`](../sql/agent-design-full-postgresql.sql:45) | [`AgentSkill`](../../src/main/java/com/simple/ai/common/entity/agentSkill/AgentSkill.java:36) | 已直接包含[`agentId`](../../src/main/java/com/simple/ai/common/entity/agentSkill/AgentSkill.java:47)，不得再新增关联表。 |
| 智能体规则 | [`agent_rule`](../sql/agent-design-full-postgresql.sql:73) | [`AgentRule`](../../src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java:36) | 已直接包含[`agentId`](../../src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java:47)，不得再新增关联表。 |
| 子智能体关联 | [`sub_agent_relation`](../sql/agent-design-full-postgresql.sql:101) | [`SubAgentRelation`](../../src/main/java/com/simple/ai/common/entity/subAgentRelation/SubAgentRelation.java:36) | 通过主智能体和子智能体 ID 关联，可供后续调度子智能体。 |
| 智能体记忆 | [`agent_memory`](../sql/agent-design-full-postgresql.sql:125) | [`AgentMemory`](../../src/main/java/com/simple/ai/common/entity/agentMemory/AgentMemory.java:36) | 用于按智能体和触发条件匹配可复用经验。 |
| 智能体记忆详情 | [`agent_memory_detail`](../sql/agent-design-full-postgresql.sql:155) | [`AgentMemoryDetail`](../../src/main/java/com/simple/ai/common/entity/agentMemoryDetail/AgentMemoryDetail.java:36) | 用于表达记忆步骤链、判断、原子命令、循环开始和循环结束。 |
| 原子命令 | [`atomic_command`](../sql/agent-design-full-postgresql.sql:195) | [`AtomicCommand`](../../src/main/java/com/simple/ai/common/entity/atomicCommand/AtomicCommand.java:36) | 保存命令与作用，默认执行器首期不得执行高风险系统命令。 |
| 任务 | [`task`](../sql/agent-design-full-postgresql.sql:221) | [`Task`](../../src/main/java/com/simple/ai/common/entity/task/Task.java:36) | 已包含执行状态和失败原因，作为一次命令调度主记录。 |
| 任务详情 | [`task_detail`](../sql/agent-design-full-postgresql.sql:263) | [`TaskDetail`](../../src/main/java/com/simple/ai/common/entity/taskDetail/TaskDetail.java:36) | 记录每个执行步骤和请求响应内容。 |

## 设计图缺口复核清单

本节用于持续对比设计文档与[`设计图/类图1.jpg`](../../设计图/类图1.jpg)，所有缺口必须独立标记状态。新对话恢复时，优先从本节第一个未完成项继续，避免只看总计划而遗漏设计图差异。

| 状态 | 设计图要点 | 当前文档或代码表现 | 后续处理 |
|---|---|---|---|
| [x] | 智能体定义直接聚合规则、技能、子智能体关系、记忆 | 总计划和执行进度已明确规则、技能通过 agentId 直属智能体，子智能体关系进入上下文 | 保持当前方向，不恢复旧关联表 |
| [x] | 智能体记忆与任务存在同步关系 | 当前实现已支持探索成功后沉淀记忆，并可由任务详情反向生成记忆详情 | 后续增强时继续保证任务详情到记忆详情字段映射一致 |
| [x] | 智能体记忆详情可关联原子命令 | 当前通过步骤执行内容和默认执行器进行安全执行，未直接新增外键字段 | 保持设计图“如果存在则引用”的弱关联语义，后续专用执行器按命令内容或角色匹配 |
| [x] | 任务详情包含父任务、子任务和下一任务链路 | 当前任务详情已有 parentTaskId、nextTaskId，子智能体协作仍为后续增强项 | 子智能体递归调度时补齐子任务创建与父子链路写入 |
| [x] | 专用原子命令执行器体系不足 | 已补充 atomicCommandRole 传递、注册表默认兜底后置、只读信息类专用执行器 | 后续可继续扩展写入类、工具调用类、子智能体类专用执行器 |
| [x] | 子智能体关系尚未形成实际递归调度 | 已在 DefaultCommandDispatchService 中补充子智能体命令识别、子请求构建、递归深度上限和父子任务链路写入 | 后续继续抽象为 SUB_AGENT 专用原子命令执行器 |
| [x] | Spring AI token 级流式输出未实现 | 已新增 AgentAiClient.chatStream 默认扩展点，并通过 AI_TOKEN 进度事件兼容阶段级事件；已在 SpringAiAgentAiClient 中接入 Spring AI 1.0.0 stream().content() 原生流式 API | 保持 Spring AI 原生 token 粒度输出，后续如模型配置支持即可通过 HTTP SSE 和 WebSocket 消费 AI_TOKEN 事件 |
| [x] | 写入类和工具类原子命令执行器缺失 | 已新增 WRITE 写入类安全执行器和 TOOL 工具类安全执行器，不直接执行高风险命令 | 后续按白名单接入真实写入和工具能力 |
| [x] | 判断步骤和循环步骤没有真实语义执行 | 已按 branchCondition 判断分支命中，循环结束支持回跳路由、退出条件和最大循环次数 | 保持 MAX_LOOP_COUNT 安全上限 |
| [x] | 失败详情去重策略仍可精细化 | 已在外层失败兜底落库前查询任务失败详情，存在失败记录时不重复写入 | 保持步骤失败详情优先，外层兜底只补缺失记录 |
| [x] | 候选记忆详情查询层状态过滤不彻底 | 已将 findAllByAgentMemoryIds 批量查询限定 Status.ON | 调度层保留二次过滤，形成双层保护 |

## 已完成事项记录

| 状态 | 事项 | 产物 | 备注 |
|------|------|------|------|
| [x] | 读取 requirement-planner 技能文档 | [`C:/Users/Admin/.kilocode/skills/requirement-planner/SKILL.md`](C:/Users/Admin/.kilocode/skills/requirement-planner/SKILL.md) | 已按需求分析、流程图、源码验证、计划输出流程执行。 |
| [x] | 读取旧执行进度文档 | [`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) | 已发现旧文档仍记录关联表与过期下一步。 |
| [x] | 读取旧开发计划文档 | [`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md) | 已确认旧计划中扩展关联表方向与新 SQL 冲突。 |
| [x] | 读取最新 PostgreSQL 设计脚本 | [`doc/sql/agent-design-full-postgresql.sql`](../sql/agent-design-full-postgresql.sql) | 已确认九张核心表和字段定义。 |
| [x] | 分析最新设计图 | [`设计图/类图1.jpg`](../../设计图/类图1.jpg) | 已确认规则、技能均直接关联智能体，任务同步来源于记忆。 |
| [x] | 通读关键实体代码 | [`AgentDefinition`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java)、[`AgentRule`](../../src/main/java/com/simple/ai/common/entity/agentRule/AgentRule.java)、[`AgentSkill`](../../src/main/java/com/simple/ai/common/entity/agentSkill/AgentSkill.java)、[`Task`](../../src/main/java/com/simple/ai/common/entity/task/Task.java) | 当前实体已基本对齐新 SQL。 |
| [x] | 通读规则模块 CRUD 链路 | [`AgentRuleController`](../../src/main/java/com/simple/ai/controller/agentRule/AgentRuleController.java)、[`AgentRuleService`](../../src/main/java/com/simple/ai/common/service/agentRule/AgentRuleService.java)、[`MPAgentRuleView`](../../src/main/java/com/simple/ai/view/agentRule/MPAgentRuleView.java)、[`AgentRuleDao.xml`](../../src/main/resources/mapper/AgentRuleDao.xml) | 已确认 View 查询可按 agentId 过滤，可被上下文装配复用。 |
| [x] | 检索过期关联表与命令调度新增类 | [`src/main/java`](../../src/main/java) | 未检索到 AgentDefinitionRuleAssociation、AgentDefinitionSkillAssociation、CommandDispatch 等实际 Java 文件。 |
| [x] | 读取 simple-common-redis 技能文档 | [`C:/Users/Admin/.kilocode/skills/simple-common-redis/SKILL.md`](C:/Users/Admin/.kilocode/skills/simple-common-redis/SKILL.md) | Redis 会话可结合项目已有 Redis 配置实现，锁和限流优先保留 simple-common 能力空间。 |
| [x] | 读取 simple-common-websocket 技能文档 | [`C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md`](C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md) | WebSocket 入口按[`@WebSocketListening`](C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md:18)分发。 |
| [x] | 重写本文档 | [`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) | 本次修订已将执行入口切换为新表结构和当前代码。 |

## 作废事项

| 状态 | 作废内容 | 作废原因 | 后续处理 |
|---|---|---|---|
| [x] | 智能体规则关联表 AgentDefinitionRuleAssociation | 最新 SQL 与设计图均表明规则通过 agent_id 直接归属智能体。 | 不创建、不扩展、不在任务调度中引用。 |
| [x] | 智能体技能关联表 AgentDefinitionSkillAssociation | 最新 SQL 与设计图均表明技能通过 agent_id 直接归属智能体。 | 不创建、不扩展、不在任务调度中引用。 |
| [x] | 删除 AgentDefinition 的 firstPrinciple、secondRule、thirdSkill | 最新 SQL 和设计图仍保留智能体定义中的第一铁律、第二规则、第三技能。 | 不再删除，后续上下文装配时纳入智能体定义上下文。 |
| [x] | 新增数据库变更脚本用于废弃字段和新增关联表 | 当前全量 SQL 已是最新设计，旧变更方向无效。 | 如需脚本，仅输出命令调度运行所需数据初始化或索引建议，不做旧字段废弃。 |

## 阶段一：重新梳理与计划修订

- [x] 重新读取并分析[`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md)。
- [x] 重新读取并分析[`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md)。
- [x] 读取并核对[`doc/sql/agent-design-full-postgresql.sql`](../sql/agent-design-full-postgresql.sql)。
- [x] 分析设计图实体关系。
- [x] 读取核心实体代码并核对字段。
- [x] 读取规则模块 Controller → Service → View → Repository → Mapper XML 链路。
- [x] 检索并确认过期关联表代码当前不存在。
- [x] 修订[`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md)，移除过期关联表设计并改为直属 agentId 查询。
- [-] 阶段一完成后更新 Todo 状态。

## 阶段二：计划技术验证

- [x] 验证[`pom.xml`](../../pom.xml)中的 Spring AI、simple-common-redis、simple-common-websocket 依赖适合当前 Spring Boot / Maven 体系；[`mvn -q -DskipTests dependency:tree`](../../pom.xml)执行成功。
- [x] 通过本地 Maven 仓库 jar 验证[`@WebSocketListening`](C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md:18)真实类路径为[`com.simple.common.websocket.common.annotation.WebSocketListening`](C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md:18)。
- [x] 验证 Redis 会话实现可使用当前项目 Redis 配置；[`src/main/resources/application-local.yaml`](../../src/main/resources/application-local.yaml)已存在[`spring.data.redis`](../../src/main/resources/application-local.yaml:19)配置。
- [x] 验证所有计划引用的 Entity、DTO、View、Repository、Mapper XML 真实存在；新增类尚未创建，将在阶段三逐步落地并逐项标记。
- [x] 验证执行计划不存在“先删除字段再使用字段”等时序矛盾；当前计划保留[`AgentDefinition.firstPrinciple`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java:59)、[`AgentDefinition.secondRule`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java:65)、[`AgentDefinition.thirdSkill`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java:71)。
- [x] 验证完成后将阶段二结论写入本文档。

## 阶段三：代码实现与逐步标记

### 命令调度 DTO 与上下文对象

- [x] 新增[`CommandDispatchRequest`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java)，字段包含 agentId、commandName、commandContent、sessionId、requestParams。
- [x] 新增[`CommandDispatchResponse`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchResponse.java)，字段包含 taskId、execStatus、responseContent、failureReason。
- [x] 新增[`AtomicCommandInvokeRequest`](../../src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeRequest.java)，字段包含 taskId、taskDetailId、atomicCommandId、atomicCommandRole、commandContent、requestParams。
- [x] 新增[`AtomicCommandInvokeResponse`](../../src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeResponse.java)，字段包含 success、responseContent、failureReason。
- [x] 新增[`CommandDispatchEvent`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchEvent.java)，用于 WebSocket 或后续事件推送。
- [x] 新增[`AgentContext`](../../src/main/java/com/simple/ai/common/dto/agent/AgentContext.java)，封装智能体定义、规则正文、技能正文、记忆摘要和会话摘要。
- [x] 新增[`AgentAiRequest`](../../src/main/java/com/simple/ai/common/dto/agent/AgentAiRequest.java)与[`AgentAiResponse`](../../src/main/java/com/simple/ai/common/dto/agent/AgentAiResponse.java)。

### 枚举与常量

- [x] 新增[`AgentExecutionStatusProcess`](../../src/main/java/com/simple/ai/common/enums/AgentExecutionStatusProcess.java)，值包含等待执行、执行中、执行成功、执行失败。
- [x] 新增[`AgentStepTypeProcess`](../../src/main/java/com/simple/ai/common/enums/AgentStepTypeProcess.java)，值包含判断、原子命令、循环开始、循环结束。
- [x] 新增[`AgentIronRuleConstant`](../../src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java)，保存系统级强制铁律；同时保留[`AgentDefinition.firstPrinciple`](../../src/main/java/com/simple/ai/common/entity/agentDefinition/AgentDefinition.java:59)参与上下文装配。

### AI、会话与上下文服务

- [x] 新增[`AgentAiClient`](../../src/main/java/com/simple/ai/common/service/agent/AgentAiClient.java)接口。
- [x] 新增[`SpringAiAgentAiClient`](../../src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java)实现，固定使用 Spring AI。
- [x] 新增[`AgentSessionService`](../../src/main/java/com/simple/ai/common/service/session/AgentSessionService.java)接口。
- [x] 新增[`RedisAgentSessionService`](../../src/main/java/com/simple/ai/service/session/RedisAgentSessionService.java)，保存会话历史与摘要。
- [x] 新增[`AgentContextAssembler`](../../src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java)，按系统铁律、智能体定义铁律/规则/技能、直属规则、直属技能、会话摘要、候选记忆顺序装配上下文。
- [x] 新增[`AgentMemoryMatcher`](../../src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java)，按智能体 ID 与触发条件匹配候选记忆。
- [x] 新增[`AgentMemorySummarizer`](../../src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java)，成功后沉淀最短命令路径。

### 原子命令执行服务

- [x] 新增[`AtomicCommandExecutor`](../../src/main/java/com/simple/ai/common/service/command/AtomicCommandExecutor.java)接口。
- [x] 新增[`AtomicCommandExecutorRegistry`](../../src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java)。
- [x] 新增[`DefaultAtomicCommandExecutor`](../../src/main/java/com/simple/ai/service/command/DefaultAtomicCommandExecutor.java)。
- [x] 确认默认执行器不执行高风险系统命令，仅返回标准执行结果或记录待人工处理。
- [x] 修改[`AtomicCommandInvokeRequest`](../../src/main/java/com/simple/ai/common/dto/command/AtomicCommandInvokeRequest.java)，新增 atomicCommandRole 字段承接 atomic_command.role。
- [x] 修改[`DefaultCommandDispatchService`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java)，匹配原子命令后传递 atomicCommandRole。
- [x] 修改[`AtomicCommandExecutorRegistry`](../../src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java)，专用执行器优先，默认执行器后置兜底。
- [x] 修改[`DefaultAtomicCommandExecutor`](../../src/main/java/com/simple/ai/service/command/DefaultAtomicCommandExecutor.java)，默认成功响应显式写入空 failureReason。
- [x] 新增[`ReadOnlyInfoAtomicCommandExecutor`](../../src/main/java/com/simple/ai/service/command/ReadOnlyInfoAtomicCommandExecutor.java)，支持 READ / QUERY / INFO 只读信息类命令。

### 统一入口与核心编排

- [x] 新增[`CommandDispatchService`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java)接口。
- [x] 新增[`DefaultCommandDispatchService`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java)，实现任务创建、上下文装配、记忆匹配、任务详情记录、失败原因记录和会话保存。
- [x] 新增[`CommandDispatchController`](../../src/main/java/com/simple/ai/controller/command/CommandDispatchController.java)，提供 HTTP 入口。
- [x] 新增[`CommandWebSocketEndpoint`](../../src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java)，使用 simple-common-websocket 的[`@WebSocketListening`](C:/Users/Admin/.kilocode/skills/simple-common-websocket/SKILL.md:18)提供 WebSocket 入口。

### 完成度复核后新增整改项

- [x] 将当前完成度 78% 与欠缺项写入[`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md)。
- [x] 补充流式调度需求：长任务执行时必须持续反馈 AI 当前步骤和执行状态。
- [x] 新增[`CommandDispatchProgressEvent`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java)，作为 HTTP SSE 与 WebSocket 共用进度事件。
- [x] 修改[`CommandDispatchService`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java)，新增流式调度方法。
- [x] 修改[`CommandDispatchController`](../../src/main/java/com/simple/ai/controller/command/CommandDispatchController.java)，新增 HTTP SSE 流式接口。
- [x] 修改[`CommandWebSocketEndpoint`](../../src/main/java/com/simple/ai/websocket/command/CommandWebSocketEndpoint.java)，按步骤持续写回进度事件。
- [x] 修改[`DefaultCommandDispatchService`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java)，在任务创建、上下文装配、记忆匹配、步骤执行、AI 执行、记忆沉淀、完成/失败各阶段发布进度事件。
- [x] 修改[`AgentMemoryMatcher`](../../src/main/java/com/simple/ai/service/agent/AgentMemoryMatcher.java)，记忆查询增加启用状态过滤。
- [x] 修改[`AgentContextAssembler`](../../src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java)，候选记忆详情仅保留启用步骤。
- [x] 修改[`DefaultCommandDispatchService`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java)，记忆详情按 nextStepId / branchRoute 游标执行并设置安全上限。
- [x] 修改[`AgentMemorySummarizer`](../../src/main/java/com/simple/ai/service/agent/AgentMemorySummarizer.java)，记忆详情沉淀改为批量写入。
- [x] 执行[`mvn clean compile`](../../pom.xml)并确认 BUILD SUCCESS；本次编译 172 个源码文件。
- [x] 执行 code-inspector 深度自检并更新最终结论。

### 设计图剩余闭环整改计划

- [x] 读取并验证子智能体递归调度相关现有代码：CommandDispatchService、DefaultCommandDispatchService、SubAgentRelationView、TaskView、TaskDetailView、AtomicCommandExecutorRegistry。
- [x] 设计子智能体命令识别规则：优先通过 atomicCommandRole 识别 SUB_AGENT，其次通过命令内容识别子智能体调用意图。
- [x] 在 DefaultCommandDispatchService 中新增子智能体递归调度私有方法，构建子 CommandDispatchRequest 并设置最大递归深度。
- [x] 在任务和任务详情写入链路中补齐父任务、子任务和下一任务关系，确保设计图中的任务链路可追踪。
- [x] 新增或扩展子智能体类原子命令执行器，按子智能体关系选择目标子智能体并调用核心调度服务；当前边界结论：通用 AtomicCommandExecutor 接口缺少父任务、进度消费者和递归深度上下文，本轮已落地 SUB_AGENT 专用识别执行器，实际递归仍由 DefaultCommandDispatchService 承接。
- [x] 新增写入类和工具类原子命令执行器骨架，保持默认安全执行器兜底不执行高风险命令。
- [x] 增强判断步骤执行逻辑，基于 branchCondition 和执行结果选择 branchRoute 或 nextStepId。
- [x] 增强循环步骤执行逻辑，设置最大循环次数和退出条件，防止死循环。
- [x] 优化失败详情去重，外层 catch 查询失败详情并只在缺失时兜底写入，避免重复写入。
- [x] 将候选记忆详情启用状态过滤下沉到批量查询方法，减少无效数据进入上下文。
- [x] 评估 Spring AI token 级流式能力，新增兼容阶段级事件的 token 输出扩展点。
- [x] 执行 [`mvn clean compile`](../../pom.xml)，确保编译通过；2026-07-10 本轮编译 BUILD SUCCESS，共编译 173 个源码文件。
 - [x] 执行 code-inspector 深度自检，递归修复后更新本文档最终状态。
 - [x] 修复自检发现的性能问题：命中记忆步骤链时，每个原子命令步骤都重复查询启用原子命令列表（N+1 DB 查询）。新增 loadEnabledAtomicCommands() 在步骤链执行前一次性加载，通过方法签名传递复用，消除循环内 DB 查询。

### 设计图剩余闭环关键文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| CommandDispatchService | [`src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java) | 按需修改 | 如递归调度需要携带内部上下文，可扩展内部方法边界 |
| DefaultCommandDispatchService | [`src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java) | 修改 | 补齐子智能体递归、判断循环真实语义、失败详情去重 |
| AtomicCommandExecutor | [`src/main/java/com/simple/ai/common/service/command/AtomicCommandExecutor.java`](../../src/main/java/com/simple/ai/common/service/command/AtomicCommandExecutor.java) | 按需修改 | 保持执行器标准接口稳定 |
| AtomicCommandExecutorRegistry | [`src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java`](../../src/main/java/com/simple/ai/service/command/AtomicCommandExecutorRegistry.java) | 修改 | 支持更多专用执行器优先级 |
| SubAgentAtomicCommandExecutor | [`src/main/java/com/simple/ai/service/command/SubAgentAtomicCommandExecutor.java`](../../src/main/java/com/simple/ai/service/command/SubAgentAtomicCommandExecutor.java) | 新增 | 识别 SUB_AGENT / 子智能体命令，配合核心服务执行递归调度 |
| WriteAtomicCommandExecutor | [`src/main/java/com/simple/ai/service/command/WriteAtomicCommandExecutor.java`](../../src/main/java/com/simple/ai/service/command/WriteAtomicCommandExecutor.java) | 新增 | 识别 WRITE / SAVE / UPDATE / DELETE 写入类命令，保持白名单前置安全边界 |
| ToolAtomicCommandExecutor | [`src/main/java/com/simple/ai/service/command/ToolAtomicCommandExecutor.java`](../../src/main/java/com/simple/ai/service/command/ToolAtomicCommandExecutor.java) | 新增 | 识别 TOOL / CALL / EXECUTE 工具类命令，保持默认安全响应 |
| SubAgentRelationView | [`src/main/java/com/simple/ai/common/view/subAgentRelation/SubAgentRelationView.java`](../../src/main/java/com/simple/ai/common/view/subAgentRelation/SubAgentRelationView.java) | 复用 | 查询主智能体和子智能体关系 |
| AgentMemoryDetailView | [`src/main/java/com/simple/ai/common/view/agentMemoryDetail/AgentMemoryDetailView.java`](../../src/main/java/com/simple/ai/common/view/agentMemoryDetail/AgentMemoryDetailView.java) | 修改 | 增加或调整启用状态过滤批量查询 |
| MPAgentMemoryDetailView | [`src/main/java/com/simple/ai/view/agentMemoryDetail/MPAgentMemoryDetailView.java`](../../src/main/java/com/simple/ai/view/agentMemoryDetail/MPAgentMemoryDetailView.java) | 修改 | 将 status 条件下沉到批量查询 |
| SpringAiAgentAiClient | [`src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java`](../../src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java) | 后续修改 | 扩展 token 级 stream 能力 |
| CommandDispatchProgressEvent | [`src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java) | 按需修改 | 承载子任务、token 或工具执行事件 |
| doc/dev-plan/agent-command-dispatch-execution-progress.md | [`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) | 修改 | 唯一可恢复执行入口 |

### 当前完成度复核记录

- 当前代码相对设计图与设计文档完成度为 84%。
- 数据模型与基础 CRUD 完成度约 95%。
- 命令调度主链路完成度约 85%。
- 阶段级流式反馈完成度约 80%。
- 记忆复用与沉淀完成度约 82%。
- 原子命令执行体系完成度约 65%。
- 子智能体协作完成度约 35%。
- 工程编译状态完成度为 100%，最近一次 [`mvn clean compile`](../../pom.xml) 为 BUILD SUCCESS。

### 数据库与文档

- [ ] 不再新增规则关联表与技能关联表脚本。
- [ ] 按需新增[`doc/sql/agent-command-dispatch-postgresql.sql`](../sql/agent-command-dispatch-postgresql.sql)，仅包含索引、初始化样例或运行辅助 SQL。
- [ ] 每完成一个代码文件或一组强相关文件，立即更新本文档对应状态。

### 编译与修复

- [x] 执行[`mvn clean compile`](../../pom.xml)。
- [x] [`mvn clean compile`](../../pom.xml)已执行成功，编译输出为 BUILD SUCCESS，当前共编译 172 个源码文件。
- [x] 当前无编译错误需要修复。
- [x] 编译成功后更新本文档编译结论。

## 阶段四：网页分析

- [x] 本次用户未提供 URL，不触发网页分析。

## 阶段五：深度自检

- [x] 读取 code-inspector 技能文档。
- [x] 通读本次新增和修改的全部文件。
- [x] 检查一票否决项：性能风险、线程安全、内存风险、链式调用、注释规范、SQL 规范、业务闭环、数据流冗余。
- [x] 确认本轮子智能体递归调度改动无共享可变请求状态、无静态集合、无新增 SQL、无全限定类名滥用。
- [x] 确认子智能体递归深度已通过 MAX_SUB_AGENT_DEPTH 限制，避免子智能体关系配置环路导致无限递归。
- [x] 确认父任务 nextTaskId 会回填子任务 ID，子任务 parentTaskId 会在创建任务时落库，父子任务链路可追踪。
- [x] 确认本轮 SUB_AGENT 专用执行器无 DB/RPC 调用、无线程共享可变集合、无 SQL 变更。
- [x] 确认 AtomicCommandExecutorRegistry 仍保持专用执行器优先、DefaultAtomicCommandExecutor 后置兜底。
- [x] 确认 DefaultCommandDispatchService 已通过注册表识别 SubAgentAtomicCommandExecutor 后再执行子智能体递归调度。
- [x] 确认 SpringAiAgentAiClient 已覆写 chatStream，通过 Spring AI 1.0.0 的 stream().content() 输出 token，并同步聚合最终响应内容。
- [x] 确认 SpringAiAgentAiClient 流式实现无 DB 查询、无循环外部接口重复调用、无静态集合、无资源句柄泄漏、无 SQL 变更。
- [x] 确认 SpringAiAgentAiClient 流式实现中 StringBuilder 为方法局部变量，不存在 Spring 单例共享可变状态。
- [x] 本轮修复后已执行[`mvn clean compile`](../../pom.xml)，BUILD SUCCESS，当前共编译 175 个源码文件。
- [x] 自检通过后更新本文档最终结论。

## 当前阻塞与风险记录

| 状态 | 风险 | 影响 | 处理 |
|---|---|---|---|
| [ ] | Maven 外部仓库依赖解析可能失败 | 可能导致 Spring AI 或 simple-common 依赖无法下载 | 编译阶段如失败，记录具体 Maven 输出并按依赖可用性调整。 |
| [ ] | simple-common-websocket 注解类路径需通过编译确认 | 若真实包名与技能文档不一致，WebSocket 入口会编译失败 | 阶段二搜索或阶段三编译时确认。 |
| [ ] | 当前基础代码生成器存在未使用 import | 可能触发静态检查风险，但 Maven compile 通常不失败 | 深度自检阶段集中清理本次新增代码，非本次生成代码按必要范围处理。 |

## 当前恢复入口

如果会话中断，下一次应按以下顺序恢复：

- 优先读取本文档：[`doc/dev-plan/agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md)。
- 再读取修订后的总计划：[`doc/dev-plan/agent-command-dispatch-plan.md`](agent-command-dispatch-plan.md)。
- 当前已完成：HTTP SSE 流式调度、WebSocket 流式事件、流式进度 DTO、记忆启用状态过滤、记忆详情执行前过滤、记忆详情批量沉淀、记忆步骤 nextStepId / branchRoute 游标执行、最大步数和循环路由保护、只读信息类专用原子命令执行器、写入类和工具类安全执行器、子智能体递归调度与父子任务链路、SUB_AGENT 专用识别执行器、失败详情去重、候选记忆详情查询层启用状态过滤、Spring AI token 级流式输出，并已通过[`mvn clean compile`](../../pom.xml)。
- 当前进行中：本轮 code-inspector 深度自检已完成；最新编译 BUILD SUCCESS，共编译 175 个源码文件。
- 下一步执行：暂无本轮已确认编码缺口；后续如需继续增强，优先基于真实运行日志扩展写入类和工具类原子命令白名单能力。
- 后续每完成一个代码文件或一组强相关文件，必须先更新本文档状态，再执行下一项。
