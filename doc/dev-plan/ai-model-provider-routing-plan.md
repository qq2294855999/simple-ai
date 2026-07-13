# 多供应商/多模型运行时配置与切换开发计划

## 当前状态与权威恢复入口

- 本文档是"多供应商/多模型运行时配置与切换"功能的唯一恢复入口；聊天流式功能既有真实 PostgreSQL + AI SSE 联调阻塞，仍独立以 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md) 的 `[!]` 项为准，二者不得合并状态。
- 当前阶段：已完成。后端全链路、前端管理页、聊天/调度页模型选择、自动化测试和深度自检全部交付。
- 综合完成度：**95%**（仅剩聊天/调度页可选 UI 增强和外部环境联调）。

## 需求概括

实现由数据库管理的千问、DeepSeek、豆包等 OpenAI-compatible 供应商与模型配置；聊天和命令调度仅提交 `modelId`，服务端按"请求显式模型 → 智能体默认模型 → 系统默认模型 → 明确失败"解析实际模型，动态构建客户端并将不可变供应商/模型快照写入审计记录。

## 技术栈与模块范围

- 技术栈：Java 17 / Spring Boot MVC / Spring AI 1.0.0 / MyBatis-Plus / PostgreSQL / React 18 / TypeScript / Ant Design。
- 新增：`aiModelProvider`、`aiModel` 的 Entity、DTO、Service、View、Repository、Mapper XML、Controller、前端 API/DTO/管理页。
- 修改：智能体定义、聊天会话/消息、任务/任务详情、命令请求/响应、AI 请求、运行时 AI 客户端、SQL、聊天与调度页面、路由与导航、智能体设计管理页。
- 不变：既有聊天 SSE 协议的消息与轨迹分流边界、外部 PostgreSQL/AI SSE 联调阻塞项。

## 业务流程

```text
用户在聊天或命令调度页选择 [智能体]
  ↓
□ 加载该智能体可用 [模型列表：供应商名称 · 模型名称]
  ↓
用户可选 [modelId]，仅提交模型主键与业务内容
  ↓
□ 服务端创建 [Task] 并解析模型选择优先级
  ↓
◇ 请求 modelId 是否存在？
  ├── 是 → □ 校验模型、供应商启用且关联一致
  └── 否 → ◇ 智能体默认 modelId 是否存在？
              ├── 是 → □ 校验可用模型
              └── 否 → ◇ 系统默认模型是否唯一可用？
                          ├── 是 → □ 选定默认模型
                          └── 否 → [明确失败：未配置可用模型]
  ↓
◇ 协议是否 OPENAI_COMPATIBLE？
  ├── 否 → [明确失败：协议不支持]
  └── 是 → □ 解密 API Key（内存短暂使用，不记录/不缓存）
              ↓
            □ 按 baseUrl、超时、modelCode 构建运行时 ChatClient
              ↓
            □ 调用模型并流式返回 token
              ↓
            □ 写入 [Task/TaskDetail/聊天消息] 的 providerId/providerName/modelId/modelCode 快照
              ↓
            [页面展示实际供应商 · 模型]
```

## 设计对齐缺口清单

| 状态 | 需求要点 | 当前代码表现 | 后续处理 |
|---|---|---|---|
| [x] | Spring AI 1.0.0 动态 OpenAI-compatible 构造 | 已通过 javap 验证 API，已实现 | 已完成。 |
| [x] | 两张表且 API Key 不重复存储 | `ai_model_provider` + `ai_model` 表，Key 仅存密文列 | 已完成。 |
| [x] | Key 加密、绝不回显或进入审计/SSE | `AiModelProviderApiKeyCipher` + 响应仅含 `apiKeyConfigured` | 已完成。 |
| [x] | 严格模型选择优先级且无 YAML 回退 | `DefaultAiModelRoutingService` 三级优先级解析 | 已完成。 |
| [x] | 历史不受后续配置修改影响 | Task/TaskDetail/AgentChatMessage 含不可变快照字段 | 已完成。 |
| [x] | 前端管理、聊天和调度模型选择 | 管理页 + 聊天页 + 调度页均已接入 | 已完成。 |
| [x] | 删除必须保护历史审计 | 模型删除检查智能体引用/任务/详情/消息四层 | 已完成。 |
| [!] | PostgreSQL/AI SSE 真实联调 | 既有聊天计划确认环境不可用 | 保持独立阻塞项。 |

## 执行状态清单

- [x] 读取权威聊天恢复入口，确认 PostgreSQL + AI SSE `[!]` 阻塞保持独立。
- [x] 完成 Controller → Service → View → Repository → Mapper XML 与前端调用链源码核验。
- [x] 完成加密能力与 Spring AI 1.0.0 动态构造 API 技术验证。
- [x] 追加全量建表、幂等增量迁移、索引、历史回填策略与执行顺序。
- [x] 新增供应商/模型 Entity、DTO、View、Repository、Mapper XML、Service、Controller，完成安全 CRUD、默认约束和删除保护。
- [x] 扩展智能体默认模型、请求 `modelId`、会话/消息/任务/详情审计快照与聚合查询。
- [x] 实现模型选择解析服务、AES-GCM 密钥保护服务及动态 OpenAI-compatible 客户端工厂。
- [x] 改造 AI 调用、命令调度、聊天与子智能体调用，使其统一经过模型路由。
- [x] 新增供应商/模型管理页、API、DTO、导航与路由，实现密钥"已配置"提示和防重复提交。
- [x] 改造聊天/命令调度页面：选智能体后加载模型，仅提交 `modelId`，展示实际使用快照。
- [x] 改造智能体设计管理页：接入真实模型下拉，字段从 `model` 迁移到 `defaultModelId`。
- [x] 增加隔离单元/契约测试（23 项 JUnit 5，含路由优先级 7 项 + 密钥安全 6 项）。
- [x] 执行 `mvn clean compile`、`mvn test`、`npm test`、`npm run build`。
- [x] 执行 code-inspector 深度自检，十三维零违规。

## 构建记录

| 阶段 | 命令 / 检查 | 结果 | 说明 |
|---|---|---|---|
| 阶段一 | 恢复文档、全链路源码、SQL、前端页面核验 | 通过 | 已确认固定客户端和模型字段失效根因。 |
| 阶段二 | `javap` 检查本地 Spring AI 1.0.0 JAR | 通过 | 验证 OpenAI-compatible 动态 API。 |
| 阶段三 | `mvn clean package` | 通过 | 247 源文件 + 23 测试项全部通过。 |
| 阶段三 | `npm run build` + `npm test` | 通过 | TypeScript 严格检查 + 9 项 Vitest 通过。 |
| 最终 | code-inspector 十三维 | 通过 | 零违规。 |

## 深度自检记录

- 调用链：`AiModelProvider/AiModel Controller → Service → View → Repository → Mapper XML` 全链路完整。
- 安全：API Key 仅在供应商表密文和调用局部变量存在，严禁进入响应 DTO、日志、SSE。
- 数据流：解析后的显示快照只在任务、详情和最终消息写入一次；前端只读取聚合响应，无 N+1。
- 删除闭环：模型删除检查智能体默认引用 + 任务/详情/消息审计四层；供应商删除检查关联模型。
- 性能：无循环内 DB/RPC 调用；前端模型列表全量加载（管理配置数据量级 <100）；列表查询均有 LIMIT。

## 外部运行时阻塞（独立保留）

- [!] 真实 PostgreSQL 与 AI SSE 联调仍严格遵循 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md) 的"精确恢复条件"。本功能不执行迁移、不启动依赖外部服务的应用、不伪造真实模型调用结果。
