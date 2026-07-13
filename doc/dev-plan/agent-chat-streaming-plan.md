# 人机流式对话与智能体调度过程可视化开发计划

## 当前总体状态（历史快照，当前状态以文末“当前恢复入口（权威）”为准）

- 本节记录当时运行时检查的历史事实，不作为恢复顺序或待办依据。
- 历史阻塞事实：本机到配置目标 PostgreSQL 的 `5432` 端口不可达，未安装 `psql`，且 `SPRING_AI_OPENAI_API_KEY` 未注入；因此当时未执行迁移、未启动依赖外部服务的应用、未写入任何远端数据。
- 已完成交付：已新增不含真实密钥或 Token 的可复现回归资产 `doc/http/智能体人机流式聊天回归.http`，覆盖会话创建、用户消息保存、SSE 事件、最终消息、历史读取、任务关联和 Markdown 表格/代码块输入。
- 设计结论：`task` 与 `task_detail` 持续作为调度执行审计；`agent_chat_session` 与 `agent_chat_message` 承担可检索聊天历史；Redis 仅保留可失效短期摘要，三者边界不重叠。

## 需求一句话概括

实现一个可选择智能体的持久化流式聊天界面：用户消息和最终 AI 受限 Markdown 回复独立展示，规则/技能/记忆/子智能体/原子命令/Token 等真实调度事件以结构化时间线展示，并在服务端复用既有命令调度核心完成任务审计与异常闭环。

## 技术栈与模块范围

- 技术栈：Java Spring Boot + Spring AI + MyBatis-Plus + PostgreSQL + React 18 + TypeScript + Ant Design + SSE。
- Controller：新增会话聊天 Controller；既有命令调度 Controller 保持为底层调度入口。
- Service：新增会话编排服务；扩展既有命令调度服务的事件载荷，不新建 AI 调度链路。
- View / Repository / Mapper XML：新增会话和消息完整访问链路；扩展智能体删除级联清理。
- Entity / DTO：新增会话、消息、聊天请求、历史响应、事件响应对象；补充结构化事件关联字段。
- SQL：向既有 PostgreSQL 设计脚本追加会话表、消息表及索引，并向运行辅助 SQL 追加生产迁移脚本。
- Web：新增人机对话页面、API、DTO、安全受限 Markdown 渲染组件；导航与路由增加入口。
- 配置：在锁定 `react-markdown` 与 `remark-gfm` 的精确版本后更新 `web/package.json` 和锁文件。

## 业务流程图

```text
用户选择智能体并输入问题
  ↓
□ 前端创建或选择 [会话]
  ↓
□ 后端事务保存 [用户消息(role=USER, sequence)]
  ↓
□ 复用 CommandDispatchService.dispatchStream
  ↓
□ 创建 [Task] 并推送 TASK_CREATED
  ↓
□ 装配智能体定义、规则、技能、Redis 摘要与候选记忆
  ↓
□ 推送结构化 CONTEXT / RULE / SKILL / MEMORY 轨迹事件
  ↓
◇ 是否命中可执行记忆？
  ├── 是 → □ 执行 [TaskDetail] 步骤链
  │           ├── 原子命令 → 推送 ATOMIC_COMMAND_* 事件
  │           └── 子智能体 → 推送 SUB_AGENT_* 事件
  └── 否 → □ Spring AI 流式输出 → 推送 AI_TOKEN 事件
  ↓
◇ 调度是否成功？
  ├── 是 → □ 校验并归一化受限 Markdown
  │           ↓
  │         □ 事务保存 [AI 消息(role=ASSISTANT, taskId, sequence)]
  │           ↓
  │         □ 推送 MESSAGE_COMPLETED / TASK_COMPLETED
  └── 否 → □ 事务保存 [AI 失败消息] 与失败状态
              ↓
            □ 推送 TASK_FAILED
  ↓
前端分流消费
  ├── [AI_TOKEN / MESSAGE_COMPLETED] → 对话消息流
  └── [其他结构化 eventType] → 执行轨迹时间线
```

## 已核验的现状与结论

| 范围 | 已核验文件 | 结论 |
|---|---|---|
| 流式入口 | `src/main/java/com/simple/ai/controller/command/CommandDispatchController.java` | 已有 POST SSE 流式入口，使用 `SseEmitter` 和异步 `TaskExecutor`。聊天入口应在其上复用服务层，不复制调度过程。 |
| 调度核心 | `src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java` | 已具备任务创建、上下文装配、记忆匹配、记忆步骤、原子命令、子智能体、AI Token 与成功/失败事件。需将现有文本 payload 调整为可展示的结构化 JSON 载荷，且事件不走 Markdown。 |
| AI 流式 | `src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java` | 已通过 Spring AI Flux 消费 token 并聚合最终响应，可直接复用。 |
| 系统铁律 | `src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java` | 只有通用铁律，需增加受限 Markdown 最终回复契约；服务端仍须执行格式归一化，不能只依赖提示词。 |
| 上下文 | `src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java` | 已按定义、规则、技能、子智能体、记忆、Redis 摘要组装。需增加可被事件轨迹安全展示的摘要载荷，避免输出完整系统提示词与敏感上下文。 |
| 执行审计 | `src/main/java/com/simple/ai/common/entity/task/Task.java`、`src/main/java/com/simple/ai/common/entity/taskDetail/TaskDetail.java` | Task/TaskDetail 是执行记录，缺少会话、角色、顺序、消息格式，不能作为聊天历史。 |
| 现有 CRUD 链路 | Task Controller → Service → View → Repository → Mapper，以及 TaskDetail Controller → Service → View → Repository → Mapper | 现有链路可继续承载执行审计；会话需独立完整链路。 |
| 删除闭环 | `src/main/java/com/simple/ai/service/agentDefinition/DefaultAgentDefinitionService.java` 与 `AgentDefinitionDao.xml` | 智能体删除已经清理任务和详情；新增会话/消息后必须先删消息、再删会话，避免孤儿数据。 |
| SQL | `doc/sql/agent-design-full-postgresql.sql`、`doc/sql/agent-command-dispatch-postgresql.sql` | 当前没有聊天持久化表；需要追加全量建表和增量迁移，不改写既有 Task/TaskDetail 语义。 |
| 前端流式 | `web/src/api/commandDispatchApi.ts`、`web/src/pages/CommandDispatchPage.tsx` | 已具备 POST SSE 帧读取和 token 累积，需抽出/复用解析能力，新增聊天页实现消息流和事件时间线分屏。 |
| 前端依赖 | `web/package.json` | 当前没有 Markdown 渲染依赖。需新增并锁定 `react-markdown`、`remark-gfm` 精确版本，禁止使用 HTML 渲染插件或 `rehype-raw`。 |

## 数据模型与接口契约

### 会话主表 `agent_chat_session`

- `id`：会话主键。
- `agent_id`：绑定的智能体，禁止在会话中途切换。
- `session_name`：从首条用户消息截断生成，用于历史列表。
- `last_message_at`：会话排序。
- `status`、审计字段：沿用项目通用字段。

### 消息明细表 `agent_chat_message`

- `id`：消息主键。
- `session_id`：归属会话。
- `task_id`：关联本轮调度任务；用户消息为空，AI 最终消息绑定真实任务。
- `role`：仅允许 USER、ASSISTANT、SYSTEM_ERROR；调度事件不存为消息。
- `content`：用户原文或最终 Markdown。
- `content_format`：PLAIN_TEXT、RESTRICTED_MARKDOWN。
- `sequence_no`：会话内严格递增，用唯一索引约束。
- `status`、时间、备注：通用字段。

### SSE 事件契约

- 用户可读对话事件：`MESSAGE_ACCEPTED`、`AI_TOKEN`、`MESSAGE_COMPLETED`、`CHAT_FAILED`。
- 执行轨迹事件：`TASK_CREATED`、`CONTEXT_ASSEMBLING`、`CONTEXT_ASSEMBLED`、`RULE_MATCHED`、`SKILL_SELECTED`、`MEMORY_MATCHING`、`MEMORY_MATCHED`、`MEMORY_MISSED`、`STEP_STARTED`、`STEP_COMPLETED`、`STEP_FAILED`、`SUB_AGENT_STARTED`、`SUB_AGENT_COMPLETED`、`ATOMIC_COMMAND_STARTED`、`ATOMIC_COMMAND_COMPLETED`、`AI_STARTED`、`AI_COMPLETED`、`MEMORY_SUMMARIZED`、`TASK_COMPLETED`、`TASK_FAILED`。
- 统一字段：`eventType`、`sessionId`、`taskId`、`stepId`、`stepName`、`execStatus`、`message`、`payload`、`completed`、`failureReason`。
- `payload` 为受控 JSON 摘要对象；前端仅解析白名单字段。系统提示词、完整规则正文、完整记忆正文、原始请求参数与敏感异常堆栈不得投递到轨迹。

### Markdown 安全策略

- 模型契约：在系统铁律中要求最终用户回复仅输出标题、段落、列表、引用、表格、行内代码与带语言标识的代码块；禁止 HTML、SVG、脚本、事件属性、链接协议绕过和调度事件伪装为回答。
- 后端强制：在最终消息持久化前剥离 HTML 标签及不支持块，将非法内容降级为转义文本；失败时按纯文本安全返回。该处理不承担 HTML 渲染。
- 前端强制：使用 `react-markdown` + `remark-gfm`，不开启 `rehype-raw`，不配置 HTML 白名单；代码块由自定义渲染器显示语言和 Clipboard API 复制按钮，表格容器横向滚动。依赖未加载或解析异常时以 `Typography.Paragraph` 纯文本展示。

## 设计对齐缺口清单

| 状态 | 设计图或需求要点 | 当前代码表现 | 后续处理 |
|---|---|---|---|
| [x] | Task / TaskDetail 保存执行审计 | 已存在任务、步骤、状态、请求与返回字段 | 继续复用为执行审计，不混入聊天消息。 |
| [ ] | 会话绑定 agentId，消息保存角色、内容、格式、任务、顺序、时间 | 当前仅 Redis 摘要与消息列表，无持久化聊天模型 | 新增 `agent_chat_session`、`agent_chat_message` 和完整后端链路。 |
| [ ] | 调度过程不能伪装为 AI 文本 | 当前事件为字符串 payload，命令页以表格展示 | 增加安全结构化载荷、聊天页分流渲染。 |
| [ ] | 最终回复受限 Markdown 且不能仅依赖提示词 | 系统铁律尚无格式约束，前端纯文本输出 | 增加核心铁律、后端格式归一化、React 安全渲染与纯文本降级。 |
| [ ] | 支持简单表格、代码块、复制 | 前端未安装 Markdown 依赖 | 锁定依赖、实现受控组件和 Clipboard API。 |
| [ ] | 智能体删除不遗留会话数据 | 现有级联删除不含聊天表 | 扩展会话消息与会话删除 SQL/Repository/View。 |
| [x] | 复用命令调度核心 | `CommandDispatchService.dispatchStream` 已可发布执行事件 | 聊天服务仅适配消息持久化与 SSE，调用既有服务。 |

## 阶段二技术验证

- [x] Maven 基线编译已通过；现有 `CommandDispatchController` 已使用 Spring MVC `SseEmitter`，项目不是 Gateway / WebFlux-only 环境。
- [x] 已读取 `simple-common-mp` 技能：String 主键由 `CustomIdGenerator` 自动生成，时间由 `MybatisPlusOperationHandler` 自动填充；新实体沿用现有 `@TableName`、`IdType.ASSIGN_UUID` 与 `Status` 模式。
- [x] 已查询 npm registry：锁定 `react-markdown` 10.1.0 与 `remark-gfm` 4.0.1；二者均适配 React 18，且不需要 HTML 解析插件。
- [x] `CommandDispatchService.dispatchStream` 与既有 WebSocket 入口均通过 `Consumer<CommandDispatchProgressEvent>` 消费事件；仅扩展事件字段和新增聊天适配器，不改变入口调用边界。
- [x] 会话消息采用 PostgreSQL 行级锁读取最后序号、数据库唯一索引 `(session_id, sequence_no)` 和事务写入，避免并发序号冲突；失败事务回滚用户消息与 AI 消息写入。
- [x] 智能体级联删除按“消息 → 会话 → 任务详情 → 任务 → 记忆详情 → 记忆 → 关系/规则/技能 → 智能体”排序，不存在反向外键循环。

### 阶段二验证结论

- 依赖兼容性：通过。
- 关键类存在性：通过，已核验 `SseEmitter` 使用点、调度服务、任务访问层、Spring AI 客户端、MyBatis-Plus 约定和前端 SSE 读取实现。
- 逻辑自洽性：通过。会话是持久化用户交互边界，Task 是执行审计边界，Redis 仅保留可失效短期摘要；三者职责不重叠。

## 执行状态清单

- [x] 读取并核对用户提供设计图。
- [x] 读取现有命令调度 Controller、Service、DTO、AI 客户端与上下文装配器。
- [x] 读取 Task / TaskDetail 的 Controller → Service → View → Repository → Mapper XML → Entity/DTO 链路。
- [x] 读取智能体定义聚合与级联删除链路。
- [x] 读取 SQL、前端调度页、前端依赖、路由与布局。
- [x] 输出数据模型、事件契约、安全 Markdown 策略和可恢复计划。
- [x] 完成阶段二技术验证。
- [x] 创建会话 / 消息 Entity、DTO、CopyMapper、View、Repository、Mapper XML、Service、Controller。
- [x] 扩展命令调度事件结构化载荷与聊天服务流式事务编排。
- [x] 向系统铁律加入受限 Markdown 格式契约，并实现服务端归一化。
- [x] 更新全量 SQL 与增量迁移 SQL，并补充索引和级联删除。
- [x] 增加前端 Markdown 依赖并锁定版本。
- [x] 实现聊天 API、DTO、安全 Markdown 组件、对话页面、执行时间线、路由和导航。
- [x] 执行 `mvn clean compile` 并修复全部 Java 编译问题。
- [x] 执行 `npm run build` 并修复全部前端构建问题。
- [x] 使用 code-inspector 深度自检，修复发现的问题。
- [x] 更新本计划验证记录、深度自检记录和总体恢复入口。
- [x] 核验本地 PostgreSQL 连通性、`psql` 客户端与 AI 密钥注入状态，并确认不可安全执行真实运行时回归。
- [x] 创建无密钥、无 Token 的聊天 SSE `.http` 回归资产，覆盖会话、消息、SSE、历史、任务关联和 Markdown 输入。
- [x] 核验路由、导航、聊天 API 与后端权限标识一致性，并执行本轮必要构建及聚焦自检。
- [!] 在可达且隔离的 PostgreSQL 环境执行迁移并完成真实 SSE 回归；阻塞原因、已验证事实和恢复操作见“运行时交付检查记录”。

## 开发计划

- 步骤一：完成阶段二依赖、关键类型、并发序号、删除顺序及 SSE 适配验证，并更新本计划。
- 步骤二：定义会话/消息表结构、迁移索引和数据保留边界，追加到 `doc/sql/agent-design-full-postgresql.sql` 与 `doc/sql/agent-command-dispatch-postgresql.sql`。
- 步骤三：新增会话和消息 Entity、角色/格式枚举、用户请求/会话列表/消息历史/SSE 响应 DTO 与 CopyMapper。
- 步骤四：新增会话和消息的 View、Repository、Mapper XML，实现按会话有序读取、会话聚合列表、消息批量删除和安全序号分配。
- 步骤五：新增会话服务，完成创建/读取、消息保存、序号并发控制、事务失败闭环与智能体绑定校验。
- 步骤六：新增聊天 Controller，提供会话列表、创建会话、历史消息、流式发送消息接口；前端仅提交 agentId、会话ID与用户文本。
- 步骤七：扩展调度进度事件和 `DefaultCommandDispatchService`，按实际上下文装配、规则、技能、记忆、步骤、子智能体、原子命令发布结构化安全事件。
- 步骤八：在 `AgentIronRuleConstant` 增加受限 Markdown 最终回复铁律，在会话服务实现后端 Markdown 归一化，禁止调度事件进入聊天 Markdown 内容。
- 步骤九：扩展智能体级联删除完整链路，按消息、会话、任务详情、任务、记忆详情、记忆等顺序清理相关数据。
- 步骤十：安装并锁定 React Markdown 依赖，新增受限 Markdown 渲染组件与代码复制、安全纯文本降级、表格横向滚动。
- 步骤十一：新增聊天 DTO 与 API，复用现有 SSE 帧解析机制，创建人机对话页面实现智能体选择、会话历史、用户/AI 消息流、执行事件时间线和失败状态。
- 步骤十二：更新路由与布局导航，确保工作台和命令调度既有功能保持可访问。
- 步骤十三：执行 Maven 编译，修复问题并记录结果。
- 步骤十四：执行 Web 构建，修复问题并记录结果。
- 步骤十五：执行深度代码自检，修复后复检并更新本计划。

## 重要文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| 本计划 | `doc/dev-plan/agent-chat-streaming-plan.md` | 新增 | 唯一恢复入口与执行记录。 |
| 全量 SQL | `doc/sql/agent-design-full-postgresql.sql` | 修改 | 新增聊天会话与消息建表定义。 |
| 增量 SQL | `doc/sql/agent-command-dispatch-postgresql.sql` | 修改 | 新增运行迁移、索引及注释。 |
| 系统铁律 | `src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java` | 修改 | 增加受限 Markdown 格式契约。 |
| 调度核心 | `src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java` | 修改 | 发布真实、结构化、安全的执行轨迹事件。 |
| 调度事件 DTO | `src/main/java/com/simple/ai/common/dto/command/CommandDispatchProgressEvent.java` | 修改 | 增加步骤标识与可控载荷语义。 |
| 会话模块 | `src/main/java/com/simple/ai/**/agentChat/**` | 新增 | 会话与消息的 Entity、DTO、Service、View、Repository、Mapper、Controller。 |
| 智能体删除 | `src/main/java/com/simple/ai/service/agentDefinition/DefaultAgentDefinitionService.java` | 修改 | 级联删除聊天消息与会话。 |
| 智能体 Mapper | `src/main/resources/mapper/AgentDefinitionDao.xml` | 修改 | 新增聊天删除与影响统计 SQL。 |
| Web 依赖 | `web/package.json`、`web/package-lock.json` | 修改 | 锁定 Markdown 依赖。 |
| 聊天 API | `web/src/api/agentChatApi.ts` | 新增 | 会话、历史和 SSE 对话接口。 |
| 聊天 DTO | `web/src/dto/agentChat/AgentChatDto.ts` | 新增 | 前后端聊天契约。 |
| Markdown 组件 | `web/src/components/agentChat/RestrictedMarkdownComponent.tsx` | 新增 | 安全 Markdown、表格、代码复制及降级。 |
| 对话页面 | `web/src/pages/AgentChatPage.tsx` | 新增 | 消息流与执行时间线分屏展示。 |
| 路由 | `web/src/router/AppRouter.tsx` | 修改 | 新增聊天页面路由。 |
| 导航 | `web/src/components/layout/BasicLayoutComponent.tsx` | 修改 | 新增人机对话菜单入口。 |

## 构建与自检记录

| 阶段 | 命令 / 检查 | 结果 | 说明 |
|---|---|---|---|
| 需求规划 | 设计图、SQL、调度、Task/TaskDetail、前端源码核验 | 通过 | 已确认必须新增会话与消息表，且可复用调度核心。 |
| 阶段二验证 | 依赖、关键类型、事务边界、调用链核验 | 通过 | Spring MVC SSE、MyBatis-Plus、调度服务和前端依赖均已实际核验。 |
| Java 编译 | `mvn clean compile` | 通过 | 修正原子命令参数、事件安全载荷与长事务边界后，223 个 Java 源文件编译成功。 |
| Web 构建 | `npm run build` | 通过 | TypeScript 严格检查和 Vite 生产构建成功；仅存在入口包超过 500 kB 的性能告警。 |
| 深度自检 | `code-inspector` | 通过 | 已递归复读聊天链路、调度事件、SQL 与前端流式渲染，发现项均已修复。 |

## 深度自检记录

- 调用链：`AgentChatController → AgentChatService → DefaultAgentChatService → Session/Message View → Repository → Mapper XML → Entity/DTO` 已完整核验；调度事件由 `DefaultCommandDispatchService` 集中构造并由聊天页面按事件类型分流。
- 修复：移除原子命令调用多余参数；HTML 模型输出整体实体转义；聊天 SSE 客户端断开不再影响最终消息事务；轨迹事件仅公开结构化计数摘要；SSE 多行 `data:` 与非法 JSON 帧得到兼容处理；调度外部入口暂停长事务，避免模型流式调用持有数据库事务。
- 性能与内存：会话列表限制 100、历史消息限制 1000、前端轨迹限制 300；无循环内聊天 DB 查询、无无界静态集合；子智能体递归与记忆步骤循环均有上限。
- 数据完整性：会话行锁与 `(session_id, sequence_no)` 唯一约束控制同会话并发顺序；智能体删除先删消息再删会话；调度事件不写入聊天消息。
- SQL 迁移：未连接外部 PostgreSQL，因此未执行运行时迁移；该项为环境未提供，不属于 Maven 编译或 Web 构建失败。
- 最终结论：13 项自检维度通过，已完成递归复检。

## 运行时交付检查记录

- [x] 已读取当前数据库、AI 配置、聊天 Controller → Service → View → Repository → Mapper XML、迁移 SQL、任务查询入口和前端 SSE API。
- [!] 阻塞条件：本机到 `application-local.yaml` 配置目标 PostgreSQL 的 `5432` 端口不可达，未发现 `psql` 客户端；同时 `SPRING_AI_OPENAI_API_KEY` 未注入，无法确认模型服务可调用。
- [x] 安全边界：未执行迁移脚本、未启动会写入远端 PostgreSQL 的服务、未提交任何数据库写操作；运行环境探测未输出任何配置凭据。
- [x] 路由、导航、聊天 API 和权限标识一致：`/agent-chat` 对应导航入口；前端 REST 与 SSE 路径对应 `sys/agent-chat`；后端权限分别为 `sys:agent-chat:session`、`session-list`、`message-list`、`send-stream`。当前统一 HTTP 与两类 POST SSE 均未配置鉴权头，符合界面已声明的“暂不介入登录鉴权”边界。
- [x] 已创建 `doc/http/智能体人机流式聊天回归.http`，其中 Token 为空占位，明确要求仅在启用鉴权的目标环境手工注入；覆盖受限 Markdown 表格和 `java` 代码块测试输入。
- [x] 本轮已执行 `mvn clean compile` 与 `npm run build`；二者通过。聚焦自检确认测试资产无密钥/Token、SSE 与聊天消息保持分流、未恢复静态模拟数据，迁移脚本不含 `DROP` 或 `DELETE`。
- [ ] 恢复操作：准备可达、隔离的 PostgreSQL 测试库并备份；安装或提供等价 PostgreSQL 执行器；设置有效 `SPRING_AI_OPENAI_API_KEY`、可用模型和已启用智能体；先审阅迁移脚本的既有 `ALTER`、`UPDATE` 与幂等占位插入，再执行聊天表相关变更；启动应用后从 `.http` 第 01 步顺序运行至第 05 步。
- [ ] SSE 通过标准：依次观察 `MESSAGE_ACCEPTED`、`TASK_CREATED`、至少一条结构化轨迹；AI 成功路径还必须有 `AI_TOKEN` 与 `MESSAGE_COMPLETED`，历史中保存 `USER` 和带 `taskId` 的 `ASSISTANT`，任务查询返回相同 `taskId`；失败路径必须有 `CHAT_FAILED` 和可读取的 `SYSTEM_ERROR`。

## 无外部依赖自动化验证子任务

### 当前恢复入口

- 当前阶段：阶段五，自动化验证与深度自检完成。
- [!] 首个恢复项保持不变：准备可达、隔离且已备份的 PostgreSQL 测试库，提供 PostgreSQL 执行器、有效 `SPRING_AI_OPENAI_API_KEY`、可用模型与已启用智能体后，先执行聊天表迁移，再从 `doc/http/智能体人机流式聊天回归.http` 第 01 步开始真实 SSE 联调。
- 当前已完成：聊天、调度事件安全边界、SSE 解析/分流与受限 Markdown 的无外部依赖自动化测试；真实环境联调不由这些测试替代。

### 测试业务流程

```text
[Mockito View / TransactionTemplate / DispatchService]
  → □ 保存模拟用户消息
  → □ 模拟成功、HTML 与失败调度响应
  → [最终消息角色、格式与聊天事件]

[模拟调度依赖]
  → □ 发布轨迹与 AI_TOKEN 事件
  → ◇ 是否为白名单安全摘要？
  ├── 是 → [安全结构化 payload]
  └── 否 → [空轨迹 payload]

[SSE 多行 data 帧]
  → □ JSON 解析与异常隔离
  → ◇ eventType 是否为聊天消息？
  ├── 是 → [消息流状态]
  └── 否 → [执行时间线状态]
```

### 执行状态清单

- [x] 读取并核验聊天 Controller → Service → View → Repository → Mapper XML、调度服务、前端 API、页面和 Markdown 组件。
- [x] 验证 Maven 已具备 `spring-boot-starter-test`，前端现无测试框架；测试将使用 Mockito 与精确锁定的 Vitest、jsdom、Testing Library，不启动应用或连接外部服务。
- [x] 为 `DefaultAgentChatService` 新增成功回复、HTML 降级、失败回复和事件分流单元测试。
- [x] 为 `DefaultCommandDispatchService` 新增安全进度载荷单元测试，验证 prompt、请求参数和敏感上下文不进入轨迹。
- [x] 提取前端 SSE 帧解析和聊天/轨迹分流纯函数，新增多行 data、非法 JSON、AI_TOKEN 与最终事件测试。
- [x] 为 `RestrictedMarkdownComponent` 新增 HTML 不解析、表格包装和代码复制渲染组件测试，并修复表格替换导致的无效 DOM 嵌套。
- [x] 执行 `mvn clean compile`、`mvn test`、`npm test`、`npm run build`，修复全部本地问题。
- [x] 执行 code-inspector 深度自检；先更新本计划，再同步 `agent-command-dispatch-execution-progress.md`。

### 技术验证结论

- 后端测试以接口替身覆盖 `AgentDefinitionView`、聊天 Session/Message View、`CommandDispatchService` 与 `TransactionTemplate`；不会创建 Spring 上下文，因此不触发 PostgreSQL、Redis 或 Spring AI 自动配置。
- 调度事件安全边界由 `DefaultCommandDispatchService` 的私有事件构造链路覆盖，测试通过反射仅验证纯对象载荷转换，不触发任务、AI、Redis 或数据库调用。
- 前端将把无副作用 SSE 解析和状态分流逻辑从页面/API 中导出，Vitest 在 jsdom 下模拟 Fetch、Clipboard 与组件渲染，不发送网络请求。
- 自动化测试仅验证本地代码契约，不能证明迁移、真实模型响应或真实 HTTP SSE 通道可用，真实联调 `[!]` 阻塞保持不变。

### 关键文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| 聊天服务测试 | `src/test/java/com/simple/ai/service/agentChat/DefaultAgentChatServiceTest.java` | 新增 | 使用 Mockito 验证最终消息与聊天事件。 |
| 调度服务测试 | `src/test/java/com/simple/ai/service/command/DefaultCommandDispatchServiceTest.java` | 新增 | 验证轨迹安全 payload 白名单。 |
| SSE 工具 | `web/src/utils/agentChatStreamUtil.ts` | 新增 | 无副作用的 SSE 帧解析与事件分流。 |
| SSE 工具测试 | `web/src/utils/agentChatStreamUtil.test.ts` | 新增 | 覆盖多行、非法 JSON、消息和时间线分流。 |
| Markdown 测试 | `web/src/components/agentChat/RestrictedMarkdownComponent.test.tsx` | 新增 | 覆盖 HTML、表格、代码复制。 |
| 前端测试配置 | `web/vitest.config.ts`、`web/src/test/setup.ts` | 新增 | jsdom 与 Testing Library 初始化。 |
| 前端依赖 | `web/package.json`、`web/package-lock.json` | 修改 | 精确锁定测试运行依赖和 `test` 脚本。 |
| Maven 测试插件 | `pom.xml` | 修改 | 锁定 Surefire 3.2.5，确保 JUnit 5 用例真实执行而非零测试通过。 |

### 自动化验证命令与结果

| 命令 | 结果 | 说明 |
|---|---|---|
| `mvn clean compile` | 通过 | 递归自检后编译 223 个生产源码文件成功。 |
| `mvn test` | 通过 | Surefire 3.2.5 真实执行 4 项 JUnit 5 测试，零失败、零错误。 |
| `npm test` | 通过 | Vitest 执行 2 个测试文件共 6 项，覆盖 SSE 帧、分流与 Markdown 组件。 |
| `npm run build` | 通过 | TypeScript 严格检查与 Vite 生产构建成功；仅保留既有单 bundle 超过 500 kB 告警。 |

### 自动化深度自检结论

- `AgentChatApi → consumeAgentChatSseEvents → AgentChatPage` 已统一为单一解析和分流链路；SSE 文本只解码、解析一次，不存在重复解析或格式往返。
- `AI_TOKEN` 仅进入临时 AI 消息，最终消息事件替换临时消息；非消息事件才进入最多 300 条的执行轨迹，消息与轨迹不混写。
- Java 测试通过 Mockito 模拟 View、事务与调度服务，不创建 Spring 上下文，不调用 PostgreSQL、Redis、AI 服务或网络。
- 安全载荷测试验证完整 prompt、原始请求参数和敏感上下文不会进入轨迹；测试数据仅使用本地虚拟文本，不含真实 Token、API Key、数据库地址或隐私数据。
- 受限 Markdown 未启用 HTML 解析；表格外层仅负责滚动，内层保持原生 `table`，代码复制仅调用浏览器 Clipboard API。
- code-inspector 发现并修复测试代码的全限定 `Status` 引用；递归复读后，性能、线程安全、内存、规范、流程闭环、数据流等 13 个维度全部通过。
- 真实 PostgreSQL、Redis、AI 密钥和网络 SSE 环境未被模拟为已完成；本节自动化验证不改变首个 `[!]` 联调恢复项。

## 接口契约与数据闭环验证子任务

### 执行快照（历史，当前恢复入口以文末权威章节为准）

- 本子任务的阶段一、阶段二与阶段三至阶段九均已完成；具体结果见“本轮验证记录”和“自动化深度自检结论”。
- 后续环境联调条件已统一收敛至文末“当前恢复入口（权威）”，本节不再维护下一步。

### 本轮业务流程

```text
[Controller 直接实例 + Mock AgentChatService]
  → □ 创建会话 / 查询会话 / 查询历史
  → □ 校验 R 响应和 Service 请求参数委托
  → □ 调用 SSE 入口
  → [异步 Service 事件写入 / 正常完成语义]

[Mapper XML 解析]
  → □ 校验 namespace 与 Repository 方法绑定
  → □ 校验 sessionId / agentId 参数、排序、LIMIT 与 FOR UPDATE
  → □ 校验删除 SQL 先按会话筛消息，再按 agentId 删会话

[AgentDefinitionService]
  → □ 删除聊天消息
  → □ 删除聊天会话
  → □ 删除智能体及剩余依赖
  → [无聊天孤儿数据]

[统一鉴权头来源]
  → □ Axios REST 请求读取业务 Authorization
  → □ SSE fetch 读取同一 Authorization
  → ◇ 是否启用认证？
  ├── 是 → [两种请求均携带 Bearer 头]
  └── 否 → [两种请求均不伪造 Authorization]
```

### 阶段二技术验证

- [x] 依赖兼容性：`pom.xml` 已有 `spring-boot-starter-test` 和 Surefire 3.2.5；Controller 可直接实例化并 Mock `AgentChatService`/`TaskExecutor`，不启动 Spring MVC、PostgreSQL、Redis 或 Spring AI。
- [x] 关键类存在性：已实际读取 `AgentChatController`、`AgentChatService`、会话/消息 View、Repository、Mapper XML、`DefaultAgentDefinitionService`、`AgentDefinitionView` 与前端 `http.ts`/`agentChatApi.ts`。
- [x] Mapper 可维护性：测试将以 JAXP 解析 XML，按 `namespace`、`id`、Repository 方法签名和归一化 SQL 片段建立结构契约，不仅检查字符串存在。
- [x] 鉴权策略：`http.ts` 当前未实现 Authorization；本轮不引入 JWT/mock 鉴权依赖，改为提供单一可测试的业务鉴权头读取函数并由 Axios 与 SSE fetch 同时调用。
- [x] 逻辑自洽性：智能体级联顺序已经是消息 → 会话 → 任务详情 → 任务；测试将验证消息删除发生在会话删除之前，并验证 SQL 使用受参数化 `IN` 过滤，不存在无条件全表删除。

### 执行状态清单

- [x] 步骤一：按 Controller → Service → View → Repository → Mapper XML 读取聊天链路，读取智能体级联删除、SQL、既有测试、前端 HTTP/SSE 实现，并完成阶段二验证。
- [x] 步骤二：新增后端隔离测试，覆盖聊天 Controller 的创建/列表/历史/SSE 委托语义、Mapper XML 结构契约与智能体删除聊天闭环。
- [x] 步骤三：在 `http.ts` 提取唯一业务鉴权头读取能力，令 `AgentChatApi.sendStream` 复用该能力；新增前端 fetch 请求头契约测试。
- [x] 步骤四：更新测试索引和本计划验证记录，确认测试不加载真实密钥、地址、JWT 或数据库。
- [x] 步骤五：执行 `mvn clean compile`，修复全部本地编译问题。
- [x] 步骤六：执行 `mvn test`，修复全部后端测试问题。
- [x] 步骤七：执行 `npm test` 与 `npm run build`，修复全部前端测试和构建问题。
- [x] 步骤八：运行 code-inspector 深度自检，修复发现项并递归复检。
- [x] 步骤九：将本计划更新为唯一精确恢复入口，将 `agent-command-dispatch-execution-progress.md` 收敛为摘要链接，并将静态页面计划标记为归档入口。

### 本轮关键文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| 唯一恢复入口 | `doc/dev-plan/agent-chat-streaming-plan.md` | 修改 | 记录本子任务状态、验证结论和后续精确恢复位置。 |
| 聊天控制器 | `src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java` | 不变或测试覆盖 | 创建、列表、历史和 SSE 委托契约。 |
| 聊天 Mapper | `src/main/resources/mapper/AgentChatSessionDao.xml`、`src/main/resources/mapper/AgentChatMessageDao.xml` | 不变或测试覆盖 | 排序、序号、行锁、查询参数语义。 |
| 智能体删除 | `src/main/java/com/simple/ai/service/agentDefinition/DefaultAgentDefinitionService.java`、`src/main/resources/mapper/AgentDefinitionDao.xml` | 不变或测试覆盖 | 消息先于会话清理，且 SQL 按 ids 过滤。 |
| 统一 HTTP | `web/src/api/http.ts` | 修改 | 导出可复用的业务 Authorization 头读取能力。 |
| 聊天 SSE API | `web/src/api/agentChatApi.ts` | 修改 | fetch SSE 使用与 REST 相同的鉴权头来源。 |
| 后端契约测试 | `src/test/java/com/simple/ai/...` | 新增 | Mockito/JAXP 隔离测试，不启动外部服务。 |
| 前端 API 测试 | `web/src/api/agentChatApi.test.ts` | 新增 | Mock fetch 校验 SSE 的请求头和帧消费。 |

### 设计对齐缺口清单

| 状态 | 需求要点 | 已核验实现 | 后续处理 |
|---|---|---|---|
| [x] | Controller 四个聊天契约可在无外部服务下验证 | `AgentChatControllerContractTest` 已覆盖创建、列表、历史和 SSE 委托 | 已完成。 |
| [x] | Mapper 关键约束应防止 SQL 漂移 | `AgentChatMapperContractTest` 已解析 XML 并验证排序、LIMIT、`FOR UPDATE` 与参数化删除 | 已完成。 |
| [x] | 智能体删除不产生聊天孤儿数据 | 服务测试验证消息 → 会话顺序，XML 契约验证 ids 过滤 | 已完成。 |
| [x] | REST 与 fetch SSE 认证策略一致 | 两类请求共用 `getBusinessAuthorizationHeader`，且已有带/无令牌测试 | 已完成。 |
| [x] | 不等待外部 PostgreSQL 或 AI 服务 | 已采用 Mockito、JAXP、Vitest mock 策略 | 全程禁止真实地址、密钥和网络调用。 |

### 本轮验证记录

| 验证项 | 结果 | 说明 |
|---|---|---|
| `mvn clean compile` | 通过 | 2026-07-13，223 个生产源码编译成功；仅保留既有过时 API 提示。 |
| `mvn test` | 通过 | 10 项 JUnit 5 测试，包含 Controller、服务、Mapper XML 和级联删除隔离契约。 |
| `npm test` | 通过 | 3 个 Vitest 文件、8 项测试，覆盖 SSE 帧、Markdown、SSE 鉴权头。 |
| `npm run build` | 通过 | TypeScript 与 Vite 生产构建通过；仅保留既有单 bundle 超过 500 kB 告警。 |
| `code-inspector` | 通过 | 修复级联删除测试中的全限定 Mockito 调用后，递归编译、测试和十三维审查均为零违规。 |

### 自动化深度自检结论

- 调用链：`AgentChatController → AgentChatService` 的四个 API 语义由直接实例化 Mock 测试覆盖；SSE 仅验证异步委托与完成语义，不启动真实 AI。
- SQL 契约：JAXP 在禁止外部 DTD/Schema 访问条件下解析 MyBatis XML，验证 namespace/Repository 方法绑定、会话排序、消息顺序、`LIMIT`、`FOR UPDATE`、最大序号及参数化删除范围。
- 删除闭环：`DefaultAgentDefinitionService` 测试验证聊天消息严格先于聊天会话删除；Mapper 结构验证消息删除通过目标智能体会话子查询过滤，避免无条件全表删除。
- 鉴权一致性：`http.ts` 与 `AgentChatApi.sendStream` 统一从 `localStorage.accessToken` 读取令牌；有值时均发送 `Bearer`，无值时均不伪造 `Authorization`。本轮未增加 JWT/mock 鉴权依赖。
- 性能与安全：测试无真实密钥、Token、地址或外部服务；无新增 N+1、无共享可变状态、无无界集合；SSE 客户端断开不会改变最终消息持久化的既有服务语义。

## 当前恢复入口（权威）

- 本节覆盖本文档此前所有“当前恢复入口”“首个恢复项”和运行时检查中的恢复描述；此前内容仅保留为已完成历史记录。
- 多供应商/多模型运行时配置与切换已另立权威恢复入口 [`ai-model-provider-routing-plan.md`](ai-model-provider-routing-plan.md)；其开发状态不得与本文聊天真实联调状态混合。
- 当前阶段：运行时部署就绪性与配置安全治理、全量构建验证和深度自检已完成；真实 PostgreSQL 与 AI SSE 联调仍保持为首个且唯一的 `[!]` 恢复项。
- 唯一后续待办：[!] 准备可达、隔离且已备份的 PostgreSQL 测试库，提供 PostgreSQL 执行器、有效 `SPRING_AI_OPENAI_API_KEY`、可用模型和已启用智能体；审阅 [`agent-command-dispatch-postgresql.sql`](../sql/agent-command-dispatch-postgresql.sql) 后执行聊天表迁移，再从 [`智能体人机流式聊天回归.http`](../http/智能体人机流式聊天回归.http) 第 01 步顺序运行至第 05 步。
- 真实联调通过标准：观察 `MESSAGE_ACCEPTED`、`TASK_CREATED` 和至少一条结构化轨迹；成功路径还需 `AI_TOKEN`、`MESSAGE_COMPLETED`、可读历史 `USER`/`ASSISTANT` 及相同 `taskId` 的任务查询；失败路径需 `CHAT_FAILED` 与可读取的 `SYSTEM_ERROR`。
- 文档口径：[`agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) 仅保留摘要并链接本文；静态页面计划已归档并链接本文；不得从其他文档推断待办。

## 运行时部署就绪性与配置安全治理子任务

### 业务重点与流程

```text
[外部环境变量]
  → □ 绑定 PostgreSQL、Redis、Spring AI 配置
  → ◇ 依赖可用？
  ├── 否 → [启动失败或 CHAT_FAILED，前端停止 loading]
  └── 是 → □ REST/SSE 使用统一 /api 基地址 → [聊天最终消息或失败消息]
  → □ 审阅增量 SQL 前置备份、顺序与幂等性
```

### 执行状态清单

- [x] 步骤一：以本文档为恢复入口，完整读取运行配置、Maven、Vite、HTTP/SSE、聊天 Controller → Service → View → Repository → Mapper XML 和 SQL 脚本，确认本地配置存在明文外部凭据、Vite 代理端口与 Spring Boot 不一致，以及 SSE 永不超时和前端未取消请求的确定缺陷。
- [x] 步骤二：将本地配置改为仅从环境变量绑定 PostgreSQL、Redis、Spring AI 等运行依赖，新增无敏感示例配置和部署说明，不写入真实密钥。
- [x] 步骤三：收敛 REST 与 POST SSE 的前端 API 基地址和 Vite 开发代理，增加最小构建期契约测试。
- [x] 步骤四：加固 SSE 客户端取消、服务端超时、执行器拒绝、数据库连接失败与 AI 调用失败的事件和最终消息闭环，避免 loading、半成品或成功误报。
- [x] 步骤五：审阅聊天表及 `task.agent_id` 增量迁移的顺序、幂等性、索引与备份/回滚前置条件；不执行迁移。
- [x] 步骤六：执行 `mvn clean compile`、`mvn test`、`npm test`、`npm run build`，仅验证本次改动和既有回归集。
- [x] 步骤七：执行 `code-inspector` 深度自检；先同步本计划的命令结果和精确恢复条件，再将 [`agent-command-dispatch-execution-progress.md`](agent-command-dispatch-execution-progress.md) 收敛为摘要链接。

### 已验证风险与计划技术验证

- 依赖兼容性：通过。项目为 Spring MVC，已有 `SseEmitter`、`TaskExecutor`、Spring AI OpenAI starter、Axios 和 Vite；不引入新的配置框架或前端通信库。
- 关键类存在性：通过。已读取 [`AgentChatController`](../../src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java)、[`DefaultAgentChatService`](../../src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java)、[`SpringAiAgentAiClient`](../../src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java)、[`AgentChatApi`](../../web/src/api/agentChatApi.ts) 与相关测试。
- 逻辑自洽性：通过。用户消息必须先持久化；调度、AI、Redis 或 SSE 写出失败必须以 `CHAT_FAILED` 结束客户端请求；客户端主动取消不撤销服务端既有最终消息审计。
- 安全结论：[`application-local.yaml`](../../src/main/resources/application-local.yaml) 已不含数据库、Redis、RabbitMQ、对象存储、JWT 或 AI 的真实凭据；必填项均由环境变量提供，无敏感样例与操作要求见 [`deployment-local-runtime.md`](../deployment-local-runtime.md)。
- 部署结论：[`vite.config.ts`](../../web/vite.config.ts) 与受跟踪的 [`vite.config.js`](../../web/vite.config.js) 均将 `/api` 转发至 `8000`，并由 [`API_BASE_URL`](../../web/src/api/http.ts) 统一 REST/SSE 根路径；最小 Vitest 契约已锁定该约定。
- SQL 风险：增量脚本已使用主要 DDL 的 `IF NOT EXISTS`，但含数据回填和占位插入；执行前必须备份并在事务/维护窗口审阅现存数据，不能将全量建表脚本用于已有库。

### 文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|---|---|---|---|
| 主计划 | `doc/dev-plan/agent-chat-streaming-plan.md` | 修改 | 本子任务状态、验证记录和唯一恢复条件。 |
| 本地运行配置 | `src/main/resources/application-local.yaml` | 修改 | 清除明文外部凭据，统一从环境变量读取。 |
| 配置样例 | `src/main/resources/application-local.example.yaml` | 新增 | 可提交的无敏感本地/部署配置模板。 |
| 部署说明 | `doc/deployment-local-runtime.md` | 新增 | 环境变量、启动、迁移前置和恢复步骤。 |
| Vite 配置 | `web/vite.config.ts` | 修改 | 对齐 `/api` 与本地后端端口。 |
| HTTP/SSE API | `web/src/api/http.ts`、`web/src/api/agentChatApi.ts` | 修改 | 统一基地址并支持 SSE 取消。 |
| 聊天页面 | `web/src/pages/AgentChatPage.tsx` | 修改 | 请求取消和失败消息闭环。 |
| 聊天控制器 | `src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java` | 修改 | 五分钟超时、执行器异常和 SSE 完成语义。 |
| 聊天服务 | `src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java` | 复查 | 已验证调度失败持久化为 `SYSTEM_ERROR` 并发布 `CHAT_FAILED`。 |
| 增量 SQL | `doc/sql/agent-command-dispatch-postgresql.sql` | 修改 | 追加部署安全前置说明，不执行脚本。 |

### 本轮验证记录

| 验证项 | 结果 | 说明 |
|---|---|---|
| `mvn clean compile` | 通过 | 2026-07-13，223 个生产源码编译成功；仅保留既有过时 API 提示。 |
| `mvn test` | 通过 | 10 项 JUnit 测试零失败；更新了有限 SSE 超时契约。 |
| `npm test` | 通过 | 3 个 Vitest 文件、9 项测试零失败；覆盖统一 `/api` SSE 地址及 `AbortSignal` 透传。 |
| `npm run build` | 通过 | TypeScript 严格检查与 Vite 生产构建成功；仅保留 1.31 MB 入口包的既有分包告警。 |
| 真实 PostgreSQL / AI SSE | 未执行 | 环境不可用且本子任务禁止伪造联调；未执行迁移、未启动依赖外部服务的应用。 |

### 本轮 SSE 失败边界验证

- [`AgentChatController`](../../src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java) 将 SSE 限制为五分钟；超时、`TaskExecutor` 拒绝、数据库异常或 AI 异常均以 SSE 错误终态结束，浏览器请求不会无限等待。
- [`DefaultAgentChatService`](../../src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java) 已复查：调度服务返回失败时会在短事务中保存 `SYSTEM_ERROR`，并发出 `CHAT_FAILED`；客户端断开仅停止事件投递，不撤销最终消息审计。
- [`AgentChatPage`](../../web/src/pages/AgentChatPage.tsx) 通过 `AbortController` 允许用户停止浏览器等待；非取消网络或 SSE 错误替换临时 token 消息为可读失败消息，随后刷新持久化历史和会话列表，避免卡死 loading、半成品或误报成功。

### 本轮 code-inspector 深度自检

- 调用链：`AgentChatPage → AgentChatApi → API_BASE_URL → Vite /api proxy → AgentChatController → AgentChatService → DefaultAgentChatService → View/Repository/Mapper` 已按实际代码复读；服务层失败持久化与控制器通道错误终态职责不重叠。
- 性能、线程安全与内存：无循环内数据库/远程调用；无新增共享可变集合、静态缓存或无上限消息集合；聊天时间线仍限制 300 条，历史查询仍有 SQL `LIMIT`。
- 规范与安全：公共 Controller API Javadoc 完整；新增分支前均有业务注释；无全限定类名、无超长取值链、无 SQL 拼接；配置和部署文档仅出现环境变量名，无真实密钥、Token、地址、账号或密码。
- SQL：增量 DDL/索引使用 `IF NOT EXISTS`，占位 INSERT 使用固定 ID 加 `WHERE NOT EXISTS`；`task.agent_id` 回填属于有条件数据变更，已明确要求备份、行数审阅和 DBA 维护窗口，未执行迁移。全量脚本含 `DROP TABLE`，已明确禁止用于已有库。
- 数据流：SSE 文本在 [`consumeAgentChatSseEvents`](../../web/src/utils/agentChatStreamUtil.ts) 中仅解析一次；前端取消不触发服务端回滚，最终历史由一次刷新读取，没有存储-读取回环。
- 深度自检结论：十三个维度全部通过，零违规；受跟踪 [`vite.config.js`](../../web/vite.config.js) 已与 [`vite.config.ts`](../../web/vite.config.ts) 对齐，后续修改代理时必须同步二者或调整构建产物跟踪策略。

### 精确恢复条件

- [!] 准备可达、隔离且已备份的 PostgreSQL 测试库，提供 PostgreSQL 执行器、`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、Redis 必填变量、有效 `SPRING_AI_OPENAI_API_KEY`、可用 `SPRING_AI_OPENAI_CHAT_MODEL` 与已启用智能体；审阅 [`agent-command-dispatch-postgresql.sql`](../sql/agent-command-dispatch-postgresql.sql) 后执行迁移，再从 [`智能体人机流式聊天回归.http`](../http/智能体人机流式聊天回归.http) 第 01 步顺序运行至第 05 步。


