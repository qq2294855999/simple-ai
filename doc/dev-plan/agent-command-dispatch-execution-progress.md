# 智能体命令调度执行进度摘要（已收敛）

## 文档定位

- 本文档不再维护待办、阻塞项、完成度或"下一步"，避免与唯一恢复入口产生状态冲突。
- 人机流式对话、命令调度、运行时联调、自动化验证和后续恢复状态，唯一以 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md) 为准。
- 多供应商/多模型运行时配置与切换状态，唯一以 [`ai-model-provider-routing-plan.md`](ai-model-provider-routing-plan.md) 为准。

## 已完成历史摘要

- 命令调度核心已具备 HTTP SSE、WebSocket 阶段事件、任务/任务详情审计、记忆匹配与沉淀、子智能体调度边界及安全原子命令阻断能力。
- 聊天功能已在调度核心之上新增会话与消息持久化、受限 Markdown、流式事件分流、智能体删除聊天级联清理。
- 已完成无外部依赖的接口契约与数据闭环验证。
- 已完成运行时部署就绪性与配置安全治理。

## 多供应商/多模型运行时配置与切换（已完成）

- 后端新增 `aiModelProvider`、`aiModel` 两表完整分层链路（Entity/DTO/View/Repository/Service/Controller）。
- 实现 AES-GCM 密钥加密保护、模型选择优先级解析（请求模型 > 智能体默认 > 系统默认 > 失败）。
- 实现动态 OpenAI-compatible ChatClient 工厂，替换固定单例注入。
- `SpringAiAgentAiClient` 已改造为通过 `AiModelRoutingService` 运行时解析模型。
- `Task`、`TaskDetail`、`AgentChatMessage`、`AgentAiResponse`、`CommandDispatchResponse` 均已具备审计快照。
- `AgentDefinition` 新增 `defaultModelId` 字段。
- `CommandDispatchRequest` 和 `SendAgentChatMessageRequest` 支持可选 `modelId`。
- 前端新增供应商管理页和模型管理页，含密钥"已配置"提示和防重复提交。
- 聊天页和命令调度页支持模型选择和供应商·模型快照展示。
- 后端测试：23 项 JUnit 5 测试（含 13 项新增路由优先级/密钥安全测试），全部通过。
- 前端测试：3 文件 9 项 Vitest，全部通过。
- 全量构建：`mvn clean package`、`npm run build` 通过。
- `code-inspector` 十三维深度自检零违规。

## 唯一恢复入口

- 聊天流式功能：请从 [`agent-chat-streaming-plan.md`](agent-chat-streaming-plan.md) 的"当前恢复入口（权威）"继续。
- 多供应商/多模型：请从 [`ai-model-provider-routing-plan.md`](ai-model-provider-routing-plan.md) 的"当前状态与权威恢复入口"继续。
