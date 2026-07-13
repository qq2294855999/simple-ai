# Simple AI 管理端静态页面治理与实时调度工作台开发计划（已归档）

> 本计划记录的静态页面治理与调度工作台实现已完成，**不再维护当前待办、恢复入口或运行时状态**。
>
> 人机流式聊天、调度、自动化验证及真实环境联调的唯一恢复入口为 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md)。

## 归档结论

- [`CommandDispatchPage.tsx`](../../web/src/pages/CommandDispatchPage.tsx) 已接入真实智能体/技能数据、POST SSE 进度和最终响应。
- [`AgentWorkbenchPage.tsx`](../../web/src/pages/AgentWorkbenchPage.tsx) 已接入真实摘要及近期任务数据。
- 该页面计划原有的完成记录仅用于追溯；任何新的代码任务、测试结论、运行时阻塞或后续步骤均必须更新至 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md)。

## 需求一句话概括

将命令调度页接入真实智能体、技能和 HTTP SSE 调度过程，并将智能体工作台替换为可展示全量运行任务的真实聚合统计及近期任务数据。

## 技术栈与模块范围

- 技术栈：Java + Spring Boot + MyBatis-Plus + PostgreSQL + React 18 + TypeScript + Ant Design + Axios。
- Controller：新增工作台聚合控制器；修改任务与命令调度相关控制器仅在契约必要处扩展。
- Service：新增工作台聚合服务；修改任务创建与调度任务创建，持久化任务所属智能体。
- View / Repository / Mapper XML：新增工作台统计与近期任务聚合查询；修改任务聚合查询及删除范围。
- Entity / DTO / SQL：任务增加 `agentId` 直接归属；新增工作台摘要与近期任务 DTO；修订 PostgreSQL 全量建表脚本。
- Web API / DTO / Page：新增命令调度和工作台 API、DTO；替换两个演示页面的数据加载、提交、SSE 消费与运行结果展示。

## 业务流程图

```text
用户进入命令调度页
  ↓
□ 加载启用智能体列表 → [智能体名称 + ID]
  ↓
◇ 是否已选择智能体？
  ├── 否 → □ 禁用技能选择 → 等待选择
  └── 是 → □ 按智能体加载启用技能列表 → [技能名称 + ID]
  ↓
□ 填写命令名称、命令内容、可选会话标识
  ↓
□ POST /sys/agent-command/dispatch-stream
  ↓
□ 消费 SSE 事件 → [进度事件列表 / AI 输出片段]
  ↓
◇ 任务是否完成？
  ├── 否 → □ 实时刷新进度与任务状态
  ├── 成功 → □ 展示任务编号、最终响应 → 刷新工作台数据
  └── 失败 → □ 展示任务编号、失败原因 → 刷新工作台数据

用户进入智能体工作台
  ↓
□ GET 工作台摘要 → [智能体/技能/运行中/失败待排查数量]
  ↓
□ GET 工作台近期任务 → [智能体名称、任务名称、状态、失败原因、更新时间]
  ↓
◇ 用户点击刷新？
  ├── 是 → □ 并行重新加载摘要和近期任务
  └── 否 → □ 保持真实展示结果
```

## 现状核对结论

### 已验证且可复用

| 模块 | 已验证文件 | 结论 |
|---|---|---|
| SSE 调度入口 | [`CommandDispatchController.java`](../../src/main/java/com/simple/ai/controller/command/CommandDispatchController.java) | 已有 `POST /sys/agent-command/dispatch-stream`，使用 `SseEmitter` 在任务执行器中异步输出事件。 |
| 调度核心 | [`CommandDispatchService.java`](../../src/main/java/com/simple/ai/common/service/command/CommandDispatchService.java)、[`DefaultCommandDispatchService.java`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java) | 已有 `dispatchStream`，已发布任务创建、上下文、记忆、步骤、AI token、任务成功/失败等事件。 |
| 调度请求/事件 | [`CommandDispatchRequest.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java)、[`CommandDispatchProgressEvent.java`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java) | 请求需要 `agentId`、`commandName`、`commandContent`，不含 `skillId`；事件已含任务号、类型、消息、数据、完成标记及失败原因。 |
| 智能体下拉 | [`AgentDefinitionController.java`](../../src/main/java/com/simple/ai/controller/agentDefinition/AgentDefinitionController.java)、[`AgentDefinitionDao.xml`](../../src/main/resources/mapper/AgentDefinitionDao.xml) | 已有聚合列表，响应含 `id`、`name`、`status`，可由前端过滤启用智能体。 |
| 技能下拉 | [`AgentSkillController.java`](../../src/main/java/com/simple/ai/controller/agentSkill/AgentSkillController.java)、[`AgentSkillDao.xml`](../../src/main/resources/mapper/AgentSkillDao.xml) | 已有按 `agentId`、`status` 过滤的聚合列表，响应含 `id`、`definitionDesc`、`agentName`。技能仅作为辅助上下文选择，不可伪造传入现有调度请求。 |
| 调度任务落库 | [`DefaultCommandDispatchService.java`](../../src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java) | 调度创建 `Task` 并保存原始请求 JSON，但当前 `Task` 无直接 `agentId` 字段。 |
| 任务聚合列表 | [`TaskController.java`](../../src/main/java/com/simple/ai/controller/task/TaskController.java)、[`TaskDao.xml`](../../src/main/resources/mapper/TaskDao.xml) | 现有聚合 SQL 使用 `task → agent_memory → agent_definition` 内连接，未命中记忆或尚未沉淀记忆的调度任务不会显示。 |
| 前端基础设施 | [`http.ts`](../../web/src/api/http.ts)、[`usePreventDoubleClickHook.ts`](../../web/src/hooks/usePreventDoubleClickHook.ts) | 已统一解包 `R<T>`、统一错误 Toast，且存在可复用的防重复点击 Hook。 |

### 设计图与 SQL 对齐

| 状态 | 设计图或 SQL 要点 | 当前实现 | 处理结论 |
|---|---|---|---|
| [x] | 任务记录执行状态、返回参数和失败原因 | [`task`](../../doc/sql/agent-design-full-postgresql.sql) 已包含 `exec_status`、`return_params`、`failure_reason`，调度服务已写入 | SSE 最终事件与工作台均复用这些运行字段。 |
| [x] | 智能体、技能、记忆、任务构成执行链路 | 设计图和现有调度服务均已落实 | 工作台以智能体生态和任务运行状态作为用户视图。 |
| [ ] | 调度任务必须可按智能体稳定关联展示 | 当前任务只在 `agent_memory_id` 上间接关联，探索中的新任务为空 | 为 `Task` 增加服务端写入的 `agentId`，禁止前端传递；聚合查询使用直接关联，避免 JSON 解析与遗漏。 |
| [ ] | 调度页面展示技能并提交 | 后端 `CommandDispatchRequest` 没有 `skillId`，调度核心以智能体上下文加载技能 | 页面保留“参考技能”作为受智能体约束的展示/辅助选择，不向调度接口发送该字段，避免无效请求参数。 |
| [ ] | 工作台统计与最近任务接口 | 当前无专用 Controller、Service、View、Repository、Mapper 契约 | 新增 UX 聚合接口，避免前端拼接统计或 N+1 请求。 |

## 页面与接口设计

### 命令调度页

- 表单字段：目标智能体（必填、启用数据）、参考技能（可选、按智能体过滤）、命令名称（必填）、命令内容（必填）、会话 ID（可选）。
- 请求字段裁剪：仅发送 `agentId`、`commandName`、`commandContent`、`sessionId`、`requestParams`；不发送 `skillId`、任务状态、任务 ID、时间、失败原因。
- 进度区：按接收顺序展示事件名称、消息、事件内容、失败原因；针对 `AI_TOKEN` 累积展示 AI 输出；终态事件展示任务号、任务状态和最终结果。
- 关联展示：下拉及进度表只展示智能体名称与技能定义摘要，不直接展示外键 ID。
- SSE 实现：使用浏览器 `fetch` 发起 POST 流式请求，读取 `ReadableStream` 并解析 SSE 的 `event:`、`data:` 帧；此方式可携带统一 Axios 等价的业务路径、JSON 请求体，并避免 `EventSource` 不支持 POST 的限制。

### 智能体工作台

- 摘要卡：智能体总数、启用智能体数、技能数量、运行中任务数、失败待排查任务数。
- 近期任务：任务 ID 仅用于行键且不作为主展示，展示智能体名称、任务名称、执行状态中文标签、失败原因/备注、更新时间；支持刷新。
- 后端接口：`GET /sys/agent-dashboard/summary` 与 `GET /sys/agent-dashboard/recent-tasks?size=10`。
- 查询原则：统计与近期任务均由后端聚合 SQL 一次返回；近期任务通过 `task.agent_id → agent_definition` 连接，避免页面逐行请求关联名称。

## 阶段二技术验证清单

- [ ] 确认 [`pom.xml`](../../pom.xml) 已包含 Spring MVC；`SseEmitter` 与当前 Spring Boot Web 依赖兼容，且本次不引入 Gateway 或 WebFlux 专属类。
- [ ] 确认 `Task` 实体、复制映射、建表 SQL、批量 SQL、任务创建路径均可一致地增加并维护 `agentId`。
- [ ] 确认智能体级联删除覆盖 `Task.agent_id` 直接关联任务及其 `TaskDetail`，避免新字段产生孤儿记录。
- [ ] 确认工作台 Repository 方法、Mapper XML `id`、View、Service、Controller 的类型和包路径一致。
- [ ] 确认前端 `fetch` SSE 解析不绕过统一错误语义：非 2xx 响应读取文本并通过 `ToastUtil` 告知；普通 REST 保持使用 [`http.ts`](../../web/src/api/http.ts)。
- [ ] 确认前端技能选择只用于用户辅助且不传入 [`CommandDispatchRequest`](../../src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java)，与后端现有调度模型一致。

## 执行状态清单

- [x] 读取静态页面、设计图、SQL、既有全栈计划及调度计划。
- [x] 读取命令调度、智能体、技能与任务的完整 Controller → Service → View → Repository → Mapper XML → DTO 链路。
- [x] 输出并文档化 UX 页面结构、流程、字段裁剪、接口缺口和技术验证结论。
- [x] 新增 `Task.agentId` 服务端归属字段，覆盖调度创建、手工创建、手工修改、任务聚合、智能体详情与级联删除。
- [x] 新增工作台摘要与近期任务的 Controller → Service → View → Repository → Mapper 聚合接口。
- [x] 替换命令调度页硬编码数据和 Toast 骨架，接入真实数据、POST SSE、最终响应和 500 条进度上限。
- [x] 替换工作台硬编码统计及任务记录，接入真实摘要、近期任务、刷新与 loading 状态。
- [x] 执行 `mvn clean compile`，并在自检修复后再次通过。
- [x] 执行 `npm run build`，并在自检修复后再次通过。
- [x] 执行 `code-inspector` 深度自检，修复任务更新归属、智能体详情实时任务遗漏、SSE 事件集合无上限风险后递归复检通过。

## 重要文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| 开发计划 | `doc/dev-plan/static-pages-dispatch-workbench-plan.md` | 新增 | 本任务唯一恢复入口和执行状态。 |
| 任务实体和服务 | `src/main/java/com/simple/ai/common/entity/task/Task.java`、`src/main/java/com/simple/ai/service/task/DefaultTaskService.java` | 修改 | 服务端维护任务直接智能体归属。 |
| 调度服务 | `src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java` | 修改 | 创建调度任务时写入智能体 ID。 |
| 任务和智能体 Mapper | `src/main/resources/mapper/TaskDao.xml`、`src/main/resources/mapper/AgentDefinitionDao.xml` | 修改 | 完整任务查询、智能体详情和级联删除使用直接关联。 |
| 工作台分层 | `src/main/java/com/simple/ai/{controller,common/service,service,common/view,view}/agentDashboard/` | 新增 | 摘要和近期任务聚合接口。 |
| 工作台 Mapper | `src/main/resources/mapper/AgentDashboardDao.xml` | 新增 | 受限统计与近期任务关联名称查询。 |
| SQL | `doc/sql/agent-design-full-postgresql.sql`、`doc/sql/agent-command-dispatch-postgresql.sql` | 修改 | 新字段、历史数据回填和索引。 |
| 命令调度 API / DTO | `web/src/api/commandDispatchApi.ts`、`web/src/dto/command/CommandDispatchDto.ts` | 新增 | REST/SSE 契约及流解析。 |
| 工作台 API / DTO | `web/src/api/agentDashboardApi.ts`、`web/src/dto/agentDashboard/AgentDashboardDto.ts` | 新增 | 工作台真实聚合接口。 |
| 两个页面 | `web/src/pages/CommandDispatchPage.tsx`、`web/src/pages/AgentWorkbenchPage.tsx` | 修改 | 清除硬编码演示数据并接入真实业务数据。 |

## 构建与深度自检记录

| 阶段 | 命令 / 检查 | 结果 |
|---|---|---|
| 后端构建 | `mvn clean compile` | 已通过；递归自检修复后再次通过。 |
| 前端构建 | `npm run build` | 已通过；递归自检修复后再次通过。仅保留现有 500KB 单 chunk 的 Vite 提示。 |
| 深度自检 | `code-inspector` | 十三维全部通过。 |

## 深度自检报告

```text
命令调度页表单
  ↓ [POST SSE JSON]
CommandDispatchController → CommandDispatchService → Task(agentId)
  ↓ [CommandDispatchProgressEvent]
CommandDispatchPage（最多保留 500 条进度事件）

智能体工作台
  ↓ [GET]
AgentDashboardController → AgentDashboardService → AgentDashboardView
  ↓
AgentDashboardRepository → AgentDashboardDao.xml
  ↓ [聚合 DTO]
AgentWorkbenchPage
```

| 维度 | 结论 |
|---|---|
| 性能风险 | ✅ 聚合查询一次返回名称与统计，无 N+1；近期任务有 `LIMIT`，页面事件有 500 条上限。 |
| 线程安全与内存 | ✅ 新增 Bean 不保存请求级集合；近期任务限制为 1–100。 |
| 代码与 SQL | ✅ 无 `SELECT *`、`${}` 或无界列表查询；公开 Java 接口有 Javadoc。 |
| 业务与数据流转 | ✅ 调度创建、手工创建、手工修改均由服务端维护 `agentId`。 |
| 一致性与孤儿数据 | ✅ 智能体详情、任务聚合和删除统一使用 `task.agent_id`；删除先清任务详情后清任务。 |
| 无效操作与数据冗余 | ✅ 工作台不在前端拼接统计；SSE 直接解析网络流，无中间格式与存储读取回环。 |
