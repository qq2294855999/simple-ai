# 智能对话——会话上下文管理、执行轨迹重构——全套设计文档

> 状态：已完成（全部6个阶段29个任务）
> 作者：qty
> 最后更新：2026-07-22
> 适用范围：`simple-ai` 项目智能对话子系统

---

## 目录与子文档索引

| 子文档                                                                                     | 章节范围                             | 大小  | 状态       |
|--------------------------------------------------------------------------------------------|--------------------------------------|-------|------------|
| [agent-chat-redesign-design.md](./agent-chat-redesign-design.md)                           | §1 现状分析 → §4 核心领域模型        | ~30KB | [x] 已完成 |
| [agent-chat-redesign-session-trajectory.md](./agent-chat-redesign-session-trajectory.md)   | §5 可信上下文 → §9 前端交互方案      | ~30KB | [x] 已完成 |
| [agent-chat-redesign-consistency-cleanup.md](./agent-chat-redesign-consistency-cleanup.md) | §10 事务/幂等/并发/断线重连/失败恢复 | ~4KB  | [x] 已完成 |
| [agent-chat-redesign-contracts.md](./agent-chat-redesign-contracts.md)                     | §11 迁移策略                         | ~2KB  | [x] 已完成 |
| [agent-chat-redesign-implementation.md](./agent-chat-redesign-implementation.md)           | §12 分阶段实施计划与验收标准         | ~6KB  | [x] 已完成 |
| [agent-chat-redesign-risk-appendix.md](./agent-chat-redesign-risk-appendix.md)             | §13 风险与回滚方案 + 附录 A/B        | ~7KB  | [x] 已完成 |

> 源文档：[agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)（58KB，13章+2附录，保留作为完整参考）

---

## 全局约束与依赖

- 编译命令: `mvn clean package`（Java）、`npm run build`（前端）
- 代码规范: java开发规范.md、web代码编辑规范.md
- 框架优先: simple-common 框架能力优先使用
- 非破坏性重构: 不修改 `AtomicCommandExecutor` 接口、`AgentAiClient` 接口、`AgentMemoryMatcher`、`SubAgentDispatchService`、Spring AI 内部 API

---

## 各阶段概要

### 设计分析（子文档 1-2）

- **§1 现状分析**：当前调用链路（Frontend→Controller→Service→AI Client）、关键证据来源、会话复用结论
- **§2 问题根因**：AI 上下文丢失、身份信息缺失、执行轨迹无层级
- **§3 目标架构**：ChatTurn 对话轮次模型、ExecutionEvent 执行事件模型、MemoryEvidence 记忆证据模型
- **§4 核心领域模型**：实体关系图、新增模型说明、现有模型变更
- **§5 可信上下文注入契约**：字段来源矩阵、注入时序、防客户端伪造约束
- **§6 会话管理**：ChatTurnService 上下文组装、Token 截断策略、Session Summary 治理
- **§7 执行轨迹**：ExecutionEvent 数据流、SSE 事件映射、记忆提炼流程、Reasoning Summary
- **§8 SSE 协议**：增强事件类型、前端事件路由
- **§9 前端交互**：页面布局变更、组件树、状态管理、折叠轨迹交互

### 一致性与迁移（子文档 3-4）

- **§10**：并发控制（分布式锁）、幂等性（idempotencyKey）、消息排序（sequenceNo）、数据一致性、断线重连、失败恢复
- **§11**：迁移策略

### 实施执行（子文档 5）

- **§12**：分阶段实施计划与验收标准（含多阶段步骤）

### 附录参考（子文档 6）

- **§13**：风险与回滚方案
- **附录 A**：快速参考卡片
- **附录 B**：关键文件索引
