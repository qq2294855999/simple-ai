# Agent Chat Redesign - Implementation Plan (sec12)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: 已完成（全部6个阶段）
> 最后更新：2026-07-22

---

## 12. 分阶段实施计划与验收标准

### 阶段 0：基础设施（DB 变更 + 模型）✅ 已完成

| #   | 任务                                                | 验收标准                        | 状态 |
|-----|-----------------------------------------------------|---------------------------------|------|
| 0.1 | 创建 `chat_turn` DDL                                | 表结构符合领域模型定义          | [x]  |
| 0.2 | 创建 `execution_event` DDL                          | 含 turnId 索引、eventType 索引  | [x]  |
| 0.3 | 创建 `memory_evidence` DDL                          | 含 turnId、memoryVersionId 索引 | [x]  |
| 0.4 | `ALTER TABLE agent_chat_session ADD create_user_id` | DDL 执行成功                    | [x]  |
| 0.5 | `ALTER TABLE agent_chat_message ADD turn_id`        | DDL 执行成功，历史数据兼容      | [x]  |
| 0.6 | 创建 Entity + View + Repository + Mapper XML        | `mvn clean compile` 通过        | [x]  |

### 阶段 1：可信上下文注入 ✅ 已完成

| #   | 任务                                                                                      | 验收标准                                                     | 状态 |
|-----|-------------------------------------------------------------------------------------------|--------------------------------------------------------------|------|
| 1.1 | `DefaultAgentChatService` 在 `createSession` 中填充 `createUserId`                        | `LoginUserUtils.getUserTemporary().getUserId()` 写入 session | [x]  |
| 1.2 | `DefaultAgentChatService.dispatchAgent()` 填充 `CommandDispatchRequest.userId`            | 从 LoginUserUtils 获取并赋值                                 | [x]  |
| 1.3 | `AgentContext` 新增 `userId`、`clientId`、`executorId` 字段，`AgentContextAssembler` 填充 | read_file 验证字段存在且赋值                                 | [x]  |
| 1.4 | `resolveClientIdIfAbsent()` 实现自动匹配                                                  | 查询当前用户下唯一 ACTIVE 客户端，多个时抛异常               | [x]  |
| 1.5 | `SpringAiAgentAiClient.buildUserContent()` 注入结构化上下文                               | System Prompt 包含 userId/clientId/executorId/history        | [x]  |

### 阶段 2：会话复用与多轮上下文 ✅ 已完成

| #   | 任务                                                                       | 验收标准                                     | 状态 |
|-----|----------------------------------------------------------------------------|----------------------------------------------|------|
| 2.1 | `ChatTurnService` 实现：创建轮次、加载历史消息窗口、装配 ChatML 格式上下文 | 单元测试覆盖                                 | [x]  |
| 2.2 | Token 裁剪算法实现                                                         | 按字符数估算，保留最近5轮完整内容 + 更早摘要 | [x]  |
| 2.3 | Session Summary 治理：从覆盖改为 List 追加 + 轻量摘要生成                  | Redis 中存储最近20轮摘要                     | [x]  |
| 2.4 | `DefaultChatAgentService.sendStream()` 集成 ChatTurnService                | 发消息时自动创建 turn，注入上下文            | [x]  |

### 阶段 3：执行轨迹重构 ✅ 已完成

| #   | 任务                                                                                | 验收标准                             | 状态 |
|-----|-------------------------------------------------------------------------------------|--------------------------------------|------|
| 3.1 | `ExecutionEvent` 实体 + View + Repository                                           | `mvn clean compile` 通过             | [x]  |
| 3.2 | `ExecutionEventBus`：事件发布/订阅，在 `DefaultAgentChatService` 组合消费者发布事件 | 每个原子命令执行前后发布事件         | [x]  |
| 3.3 | `AgentChatMessageResponse` 扩展 `executionEvents` 字段                              | 查询消息时 JOIN 关联 execution_event | [x]  |
| 3.4 | 前端 `AIMessageBubble` 组件：内嵌折叠执行轨迹                                       | 展开/折叠交互正常，原子命令按序展示  | [x]  |

### 阶段 4：记忆提炼 ✅ 已完成（核心蒸馏管线）

| #   | 任务                                                                                | 验收标准                                        | 状态 |
|-----|-------------------------------------------------------------------------------------|-------------------------------------------------|------|
| 4.1 | `AgentMemoryDistiller` 实现：收集 ExecutionEvent，判断沉淀条件，创建 MemoryEvidence | `mvn clean compile` 通过                        | [x]  |
| 4.2 | 实现 `triggerMemoryPrecipitation()` TODO 桩 — 委托给 `AgentMemoryDistiller`         | `DefaultAgentChatService.triggerDistillation()` | [x]  |
| 4.3 | 记忆版本管理集成（关联到现有 `agent_memory_version` 流程）                          | 端到端测试：一轮对话 → 自动生成 DRAFT 记忆版本  | [x]  |

### 阶段 5：断线重连与容错 ✅ 已完成

| #   | 任务                                            | 验收标准                      | 状态 |
|-----|-------------------------------------------------|-------------------------------|------|
| 5.1 | 前端 SSE 断线检测 + 指数退避重连                | 网络断开后自动重连            | [x]  |
| 5.2 | `GET /sys/agent-chat/turn/{turnId}/status` 接口 | 返回轮次状态                  | [x]  |
| 5.3 | 消息幂等（idempotencyKey）                      | 重复请求不产生重复消息        | [x]  |
| 5.4 | 会话级分布式锁                                  | 同一 session 并发请求排队执行 | [x]  |

### 阶段 6：清理与文档 ✅ 已完成

| #   | 任务                                                                 | 验收标准                | 状态 |
|-----|----------------------------------------------------------------------|-------------------------|------|
| 6.1 | 废弃 `GET /sys/agent-chat/trajectory/{sessionId}`（标记 deprecated） | Swagger 标注 deprecated | [x]  |
| 6.2 | 前端移除独立 Timeline 面板                                           | 两栏布局正常            | [x]  |
| 6.3 | 更新 API 文档                                                        | Swagger 文档完整        | [x]  |

---

## 编译验证记录

| 时间       | 命令              | 结果                           |
|------------|-------------------|--------------------------------|
| 2026-07-22 | mvn clean compile | BUILD SUCCESS                  |
| 2026-07-22 | npm run build     | ✓ built in 1.04s              |
| 2026-07-22 | mvn clean package | BUILD SUCCESS（阶段5-6完成后） | | 2026-07-22 | npm run build | ✓ built in 758ms（阶段5-6完成后） |
|            | 2026-07-22        | mvn clean compile              | BUILD SUCCESS（深度自检后） |

---

## 深度自检记录

> 自检时间：2026-07-22
> 自检工具：code-inspector 技能
> 审查模式：逐阶段独立深度自检（阶段0～阶段6）

| 阶段                        | 自检维度     | 通过 | 违规 | 关键修复                                                                                                                                                                      |
|-----------------------------|--------------|------|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 阶段0：基础设施             | 全部 14 维度 | 13   | 1    | `MemoryEvidence.qualityScore`、`CreateMemoryEvidenceRequest.qualityScore`、`InfoMemoryEvidenceResponse.qualityScore` 类型从 `Object` → `Double`（与 DDL `numeric(3,2)` 对齐） |
| 阶段1：可信上下文注入       | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |
| 阶段2：会话复用与多轮上下文 | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |
| 阶段3：执行轨迹重构         | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |
| 阶段4：记忆提炼             | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |
| 阶段5：断线重连与容错       | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |
| 阶段6：清理与文档           | 全部 14 维度 | 14   | 0    | —                                                                                                                                                                             |

### 自检清单

- [x] 已完整阅读所有变更代码（Entity/View/Repository/Mapper XML/DTO/CopyMapper/Service/Controller）
- [x] 已绘制调用链并与代码对照（Controller → Service → View → Repository → Mapper XML）
- [x] 已对比同类业务路径的一致性
- [x] 已检查无效操作（未使用 import 已标注）
- [x] 已检查孤儿数据（级联删除覆盖 session/message/task/taskDetail/chatTurn/executionEvent/memoryEvidence/Redis）
- [x] 已执行递归自检（阶段0发现→修复→重编译→重检→零违规）
- [x] 编译通过（mvn clean compile BUILD SUCCESS）
- [x] 链式调用合规（Entity Lombok @Accessors chain 仅用于构建，Service 层无链式调用）
- [x] 业务主方法编排合规（sendStreamInternal 按子方法调用组织）
- [x] 注释合规（Javadoc + 方法内换行注释齐全）
- [x] 已检查数据转换冗余（无冗余）
- [x] 已检查解析重复（无重复解析）
- [x] 已检查中间格式冗余（无中间格式冗余）
- [x] 已检查存储-读取回环（无回环）
- [x] 已检查公共方法调用链冗余（无回环）

### 修改类及代码行数

- `MemoryEvidence.java` | 1 行修改（qualityScore 类型）
- `CreateMemoryEvidenceRequest.java` | 1 行修改（qualityScore 类型）
- `InfoMemoryEvidenceResponse.java` | 1 行修改（qualityScore 类型）

### 最终评估

**全部 7 个阶段（0～6）深度自检通过。发现 1 处严重违规（类型不匹配），已修复并通过编译验证。**

---

