# 人机对话会话、上下文、执行轨迹重构——整体设计文档

> 状态：草案 v1.0
> 作者：qty
> 最后修订：2026-07-22
> 适用范围：`simple-ai` 项目人机对话子系统

---

## 目录

1. [现状调用链与关键证据](#1-现状调用链与关键证据)
2. [问题根因分析](#2-问题根因分析)
3. [目标架构与边界](#3-目标架构与边界)
4. [核心领域模型](#4-核心领域模型)
5. [可信上下文注入契约](#5-可信上下文注入契约)
6. [会话生命周期与上下文装配](#6-会话生命周期与上下文装配)
7. [执行轨迹与记忆提炼](#7-执行轨迹与记忆提炼)
8. [SSE 协议与事件契约](#8-sse-协议与事件契约)
9. [前端交互方案](#9-前端交互方案)
10. [事务 / 幂等 / 并发 / 断线重连 / 失败恢复](#10-事务--幂等--并发--断线重连--失败恢复)
11. [迁移策略](#11-迁移策略)
12. [分阶段实施计划与验收标准](#12-分阶段实施计划与验收标准)
13. [风险与待澄清项](#13-风险与待澄清项)

---

## 1. 现状调用链与关键证据

### 1.1 完整调用链（基于实际源码逐行验证）

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  FRONTEND (AgentChatPage.tsx)                                                │
│  handleSend() → buildOptimisticUserMessage() → setAiThinking(true)           │
│    → AgentChatApi.sendStream(request, handleProgress, abortSignal)           │
│      → fetch POST /sys/agent-chat/send-stream (SSE)                          │
│        headers: Accept: text/event-stream, Content-Type: application/json     │
│        body: { sessionId, content, modelId?, clientId? }                      │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  Controller: AgentChatController.sendStream()                                │
│  - new SseEmitter(300000ms timeout)                                          │
│  - emitter.onTimeout → completeWithError(TimeoutException)                   │
│  - taskExecutor.execute(() -> runChatStream(request, emitter))               │
│    → runChatStream: agentChatService.sendStream(request, eventConsumer)      │
│      → emitter.send(SseEmitter.event().name(eventType).data(event))          │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  Service: DefaultAgentChatService.sendStream()                               │
│                                                                              │
│  Phase 1: saveUserMessage(request) [TRANSACTION]                            │
│    - SELECT ... FROM agent_chat_session WHERE id=? FOR UPDATE                 │
│    - AssertUtils.notEmpty(session) + status==ON 校验                          │
│    - nextSequenceNo = findMaxSequenceNo(sessionId) + 1                       │
│    - INSERT agent_chat_message (role=USER, sequenceNo, content)               │
│    - UPDATE agent_chat_session (sessionName=首条截断, lastMessageAt=now)      │
│    - publishChatEvent("MESSAGE_ACCEPTED") → SSE                              │
│                                                                              │
│  Phase 2: dispatchAgent(session, request, eventConsumer)                    │
│    - 构建 CommandDispatchRequest:                                            │
│        agentId=session.agentId, commandName="人机对话",                       │
│        commandContent=request.content, sessionId=session.id,                  │
│        modelId=request.modelId, clientId=request.clientId                     │
│    - ⚠ userId 字段未填充（CommandDispatchRequest 有此字段但从未被赋值）       │
│    - commandDispatchService.dispatchStream(dispatchRequest, eventConsumer)    │
│                                                                              │
│  Phase 3: saveFinalMessage(session, response, eventConsumer) [TRANSACTION]  │
│    - SELECT ... FOR UPDATE 重新锁定 session                                  │
│    - nextSequenceNo = findMaxSequenceNo + 1                                   │
│    - INSERT agent_chat_message (role=ASSISTANT/SYSTEM_ERROR, taskId,         │
│        content, providerId, providerName, modelId, modelCode)                │
│    - publishFinalEvent("MESSAGE_COMPLETED"/"CHAT_FAILED") → SSE             │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  Service: DefaultCommandDispatchService.dispatchStream()                     │
│  @Transactional(propagation = NOT_SUPPORTED)  ← 注意：调度本身无事务！       │
│                                                                              │
│  executeDispatchInternal(request, progressConsumer, parentTaskId, depth):    │
│    1. 校验 agentId / commandName / commandContent                             │
│    2. resolveClientIdIfAbsent(request) → ⚠ TODO 桩，未实现自动匹配             │
│    3. createRunningTask(request, parentTaskId):                               │
│       - new Task(), set agentId/commandName/stepType=ATOMIC_COMMAND           │
│       - taskView.save(task) ← Task 实体落库                                   │
│       - SSE event: "TASK_CREATED"                                            │
│    4. AgentContext context = agentContextAssembler.assemble(request):         │
│       - 从 DB 加载 AgentDefinition(含 firstPrinciple/systemPrompt)            │
│       - 加载 Rules / Skills / SubAgentRelations / Memories / MemoryDetails    │
│       - 从 Redis 加载 sessionSummary (RedisAgentSessionService.findSummary)   │
│       - 组装 promptContent = definition.systemPrompt                          │
│       - SSE events: CONTEXT_ASSEMBLING → CONTEXT_ASSEMBLED/RULE_LOADED/...  │
│    5. memories = agentMemoryMatcher.match(request)                           │
│       - SSE events: MEMORY_MATCHING → MEMORY_MATCHED/MEMORY_MISSED           │
│    6. executeCommand(task, request, context, memories, ...):                 │
│       - 有命中记忆 → executeMemorySteps() 步骤链循环                          │
│       - 无命中记忆 → executeAiExploration()                                   │
│    7. saveSessionSummary(request, responseContent):                           │
│       - agentSessionService.saveSummary(sessionId, responseContent)           │
│         → Redis SET simple-ai:agent:session:summary:{id} ← 覆盖式！          │
│       - agentSessionService.appendMessage(sessionId, request.commandContent) │
│         → Redis RPUSH + LTRIM(保留最近100条)                                  │
│    8. markTaskSuccess/failed → taskView.updateById(task)                     │
│    9. triggerMemoryPrecipitation(): ⚠ TODO 桩，记忆沉淀未实现                  │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                        ┌───────────┴───────────┐
                        ▼                       ▼
          ┌──────────────────────┐   ┌──────────────────────┐
          │ executeAiExploration │   │ executeMemorySteps   │
          │                      │   │                      │
          │ buildAiRequest():     │   │ 步骤链循环:           │
          │   promptContent       │   │   while(current) {    │
          │   + commandContent    │   │     executeAtomicCmd  │
          │   + sessionSummary    │   │     saveTaskDetail    │
          │                      │   │     findNextDetail    │
          │ ⚠ 无多轮对话历史！     │   │   }                  │
          │ agentAiClient         │   │                      │
          │   .chatStream()       │   │ 子智能体递归:         │
          │   → Spring AI         │   │   subAgentDispatch    │
          │     + toolCallbacks   │   │   .dispatch()        │
          │                      │   │                      │
          │ SSE: AI_TOKEN 事件     │   │ SSE: STEP_STARTED,   │
          │ 逐个 token 流式推送     │   │  STEP_COMPLETED     │
          │                      │   │                      │
          │ saveAiTaskDetail()    │   │ saveTaskDetail()     │
          │ triggerMemoryPrecip() │   │ 每个步骤一次落库       │
          └──────────────────────┘   └──────────────────────┘
```

### 1.2 关键证据来源

| 证据点                  | 源码位置                                                                                                                                                                                                                                                |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 会话创建与复用          | [`DefaultAgentChatService.createSession()`](src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java:100) — 创建 `agent_chat_session` 记录；`sendStream()` 使用 `request.sessionId` 复用                                              |
| userId 未填充           | [`DefaultAgentChatService.dispatchAgent()`](src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java:330) — 构建 `CommandDispatchRequest` 时仅设置了 agentId/commandName/commandContent/sessionId/modelId/clientId，**未设置 userId** |
| clientId 自动匹配未实现 | [`DefaultCommandDispatchService.resolveClientIdIfAbsent()`](src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java:1479) — 明确标注 `// TODO: 未指定时自动匹配唯一在线客户端`                                                   |
| AI 上下文无多轮历史     | [`SpringAiAgentAiClient.buildUserContent()`](src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java:159) — 仅拼接 `promptContent + "\n\n" + commandContent`                                                                               |
| Session Summary 覆盖式  | [`RedisAgentSessionService.saveSummary()`](src/main/java/com/simple/ai/service/session/RedisAgentSessionService.java:57) — `stringRedisTemplate.opsForValue().set(key, summary, 24h)`，每次覆盖                                                         |
| 消息仅存用户侧          | [`DefaultCommandDispatchService.saveSessionSummary()`](src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java:1458) — `agentSessionService.appendMessage(sessionId, request.getCommandContent())` 仅追加用户命令                |
| 记忆沉淀未实现          | [`DefaultCommandDispatchService.triggerMemoryPrecipitation()`](src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java:1499) — 明确标注 `// TODO: 实现记忆沉淀逻辑`                                                              |
| 轨迹 = TaskDetail 查询  | [`DefaultAgentChatService.findTrajectory()`](src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java:178) — 从消息中收集 taskId 后批量查询 taskDetail                                                                                |
| Session 锁定机制        | [`AgentChatSessionDao.xml selectByIdForUpdate`](src/main/resources/mapper/AgentChatSessionDao.xml:4) — `SELECT ... FOR UPDATE`                                                                                                                          |
| 消息序号递增            | [`AgentChatMessageDao.xml selectMaxSequenceNo`](src/main/resources/mapper/AgentChatMessageDao.xml:13) — `COALESCE(MAX(sequence_no),0)`                                                                                                                  |

### 1.3 会话是否复用的结论

**结论：会话确实被复用。** 每次 `sendStream` 不会创建新会话，而是使用同一个 `sessionId`。证据：

1. 前端 [`AgentChatPage.handleSend()`](web/src/pages/AgentChatPage.tsx:286) 构建的 `SendAgentChatMessageRequest` 包含 `selectedSessionId`
2. 服务端 [`DefaultAgentChatService.saveUserMessage()`](src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java:283) 通过 `findByIdForUpdate`
   锁定 **现有**会话
3. `agent_chat_message` 表按 `sessionId` + `sequenceNo` 递增存储多条消息

**但上下文丢失的根因不在此**——见下一章。

---

## 2. 问题根因分析

### 2.1 痛点 1：AI 上下文丢失（"为什么窗口未关闭时上下文丢失"）

**根因：AI 调用时未注入多轮对话历史。**

```java
// SpringAiAgentAiClient.java:159
private String buildUserContent(AgentAiRequest request) {
    return request.getPromptContent() + "\n\n" + request.getCommandContent();
}
```

- `AgentAiRequest.promptContent` = 智能体定义中的 `systemPrompt`（静态）
- `AgentAiRequest.commandContent` = 当前轮用户输入
- `AgentAiRequest.sessionSummary` = Redis 中上一次被覆盖的摘要（单字符串，非结构化历史）

**缺失的关键要素：**

- 未从 `agent_chat_message` 表加载该 session 的历史消息
- 未将历史 USER/ASSISTANT 消息对组装为标准 ChatML 格式
- Redis 中的 `sessionSummary` 是每次覆盖的，不是累积的多轮摘要
- `AgentContext.sessionSummary` 只在 `executeAiExploration` 的 `buildAiRequest` 中透传，不在记忆步骤链中被消费

### 2.2 痛点 2：可信上下文缺失（userId / executorId / clientId）

| 字段                         | 当前状态                                                                                | 问题                                                   |
|------------------------------|-----------------------------------------------------------------------------------------|--------------------------------------------------------|
| `userId`（当前登录用户ID）   | `CommandDispatchRequest.userId` 字段存在但**从未被赋值**                                | AI 调用时不知道"谁在对话"，无法按用户过滤资产/校验权限 |
| `executorId`（执行器类型ID） | `AgentContext.executors` 存在，但来源是当前用户下所有启用执行器，**未按 clientId 过滤** | 无法精确告知 AI"当前使用的是哪个客户端"                |
| `clientId`                   | 前端可选传入，服务端有 TODO 桩但未自动匹配                                              | 多客户端场景下 AI 不知道该对哪个客户端下发命令         |

**三个关键时点分析：**

| 时点         | 可用身份源                                               | 应注入的上下文                                                                       |
|--------------|----------------------------------------------------------|--------------------------------------------------------------------------------------|
| **建会话**   | `LoginUserUtils.getUserTemporary().getUserId()`          | userId → `agent_chat_session.create_user_id`（会话归属）                             |
| **发消息**   | `LoginUserUtils.getUserTemporary().getUserId()` 全程可用 | userId → `CommandDispatchRequest.userId`；executorId/clientId → 从请求参数或自动匹配 |
| **执行命令** | `AtomicCommandInvokeRequest.clientId` 已透传             | 需确保 clientId 链路完整性（前端 → 调度 → 原子命令 → WebSocket）                     |

### 2.3 痛点 3：执行轨迹独立记录、独立展示

**现状：**

- 执行轨迹 = `TaskDetail` 列表，按 `taskId` 查询后在线程右侧 Timeline 展示
- 轨迹与对话消息是 **并排关系**，不是 **嵌套关系**
- 用户看不到"AI 这条回复背后调用了哪些原子命令"
- 前端 [`AgentChatPage`](web/src/pages/AgentChatPage.tsx:533) 将 `trajectories`（历史）和 `events`（实时）分开渲染

**目标：**

- 每条 AI 回复内以折叠小字显示"当前正在做什么"
- AI 对外发起的全部原子命令按实际时序构成该轮的执行轨迹
- 轨迹从属于该轮对话，而非独立展示

---

## 3. 目标架构与边界

### 3.1 架构愿景

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            目标架构总览                                       │
│                                                                             │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────────────────────────┐  │
│  │ 前端 Chat │    │ SSE/WS   │    │          后端服务                      │  │
│  │ 页面      │◄──►│ 通道     │◄──►│                                      │  │
│  │          │    │          │    │  AgentChatController                  │  │
│  │ 消息气泡  │    │          │    │    │                                 │  │
│  │  ├ AI回复 │    │          │    │    ▼                                 │  │
│  │  │  ├折叠 │    │          │    │  ChatTurnService (NEW)               │  │
│  │  │  │ 轨迹│   │          │    │    │ 管理 对话轮次、上下文装配         │  │
│  │  │  │ 原子│   │          │    │    │ token 裁剪、推理摘要治理          │  │
│  │  └──┘    │    │          │    │    ▼                                 │  │
│  └──────────┘    └──────────┘    │  CommandDispatchService               │  │
│                                   │    │ 原子命令执行、子智能体调度        │  │
│                                   │    ▼                                 │  │
│                                   │  ExecutionEventBus (NEW)              │  │
│                                   │    │ 原子命令事件发布/订阅             │  │
│                                   │    ▼                                 │  │
│                                   │  AgentMemoryDistiller (NEW)           │  │
│                                   │    │ 记忆提炼：轮次证据 → 版本管理     │  │
│                                   └──────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 核心设计原则

1. **每轮对话 = 一条 USER 消息 + 一条 ASSISTANT 消息 + N 条原子命令事件（折叠展示）**
2. **AI 上下文由服务端可信组装，客户端不可伪造**
3. **上下文注入分三个层级：会话级（userId/agentId）、轮次级（历史消息窗口）、调用级（clientId/executorId）**
4. **执行轨迹嵌套在 AI 回复消息内，不独立展示**
5. **推理链（chain-of-thought）不落库、不传输，仅输出受控的 reasoning summary/状态说明**

### 3.3 边界与不涉及范围

- **不改动** 原子命令执行器（`AtomicCommandExecutor` 接口及其实现）
- **不改动** AI 客户端核心调用（`AgentAiClient` 接口签名可扩展但需兼容）
- **不改动** 记忆匹配引擎（`AgentMemoryMatcher`）
- **不改动** 子智能体调度机制（`SubAgentDispatchService`）
- **不新增** 对 Spring AI 内部 API 的直接依赖
- **不存储** 模型原始思维链（CoT），仅存储治理后的摘要

---

## 4. 核心领域模型

### 4.1 实体关系图

```
┌──────────────────────┐
│   AgentChatSession   │  1 ──── * ┌──────────────────────┐
│                      │───────────│    ChatTurn          │
│  id                  │           │                      │
│  agentId             │           │  id                  │
│  createUserId (NEW)  │           │  sessionId            │
│  sessionName         │           │  turnNumber (NEW)    │
│  lastMessageAt       │           │  userMessageId        │
│  status              │           │  assistantMessageId   │
└──────────────────────┘           │  reasoningSummary    │  ← 受控摘要，非思维链
                                   │  status              │
                                   │  taskId (冗余)        │
                                   └──────┬───────────────┘
                                          │ 1 ──── *
                                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                      AgentChatMessage                            │
│                                                                  │
│  id, sessionId, taskId, role(USER/ASSISTANT/SYSTEM_ERROR)        │
│  content, contentFormat(PLAIN_TEXT/RESTRICTED_MARKDOWN)          │
│  sequenceNo, turnId (NEW), providerId/Name, modelId/Code          │
│  createTime, status                                              │
└──────────────────────────────────────────────────────────────────┘
                                          │
                                          │ 关联
                                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                    ExecutionEvent (NEW)                           │
│                                                                  │
│  id, turnId (NEW), taskId, taskDetailId                          │
│  eventType (ATOMIC_COMMAND_START/COMPLETE/FAILED,                │
│             AI_TOKEN, CONTEXT_ASSEMBLING, MEMORY_MATCHING,       │
│             SUB_AGENT_START/COMPLETE, etc.)                       │
│  stepName, commandName, commandContent,                           │
│  responseContent (截断), failureReason                           │
│  sequenceNo (轮次内序号), startedAt, finishedAt                  │
│  atomicCommandId, atomicCommandCode,                             │
│  providerId/Name, modelId/Code                                   │
│  createTime, status                                               │
└──────────────────────────────────────────────────────────────────┘
                                          │
                                          │ 证据引用
                                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                   MemoryEvidence (NEW)                             │
│                                                                  │
│  id, turnId, memoryVersionId (关联 agent_memory_version)         │
│  evidenceType (EXECUTION_TRACE / REASONING_SUMMARY)              │
│  evidenceContent (JSON: 原子命令调用链 + 结果摘要)                │
│  qualityScore, createTime                                        │
└──────────────────────────────────────────────────────────────────┘
```

### 4.2 新增模型说明

#### ChatTurn（对话轮次）

| 字段                 | 类型         | 说明                                                                      |
|----------------------|--------------|---------------------------------------------------------------------------|
| `id`                 | String(UUID) | 轮次主键                                                                  |
| `sessionId`          | String       | 所属会话                                                                  |
| `turnNumber`         | Integer      | 会话内轮次序号（从 1 递增）                                               |
| `userMessageId`      | String       | 该轮用户消息ID                                                            |
| `assistantMessageId` | String       | 该轮 AI 回复消息ID                                                        |
| `taskId`             | String       | 关联的调度任务ID（冗余便于查询）                                          |
| `reasoningSummary`   | String(JSON) | **受控推理摘要**：{ "intent": "...", "actions": [...], "outcome": "..." } |
| `status`             | Status       | ON/DISABLE                                                                |

**注意：** `reasoningSummary` 不包含模型原始逐字思维链，而是由服务端在 AI 调用完成后提炼的结构化摘要。其格式受模板约束，内容不包含任何模型内部独白。

#### ExecutionEvent（执行事件）

替代当前混杂在 SSE 事件 + TaskDetail 中的执行轨迹。特点：

- 每个原子命令调用产生 **2 条事件**：`ATOMIC_COMMAND_START` + `ATOMIC_COMMAND_COMPLETE/FAILED`
- AI Token 流式事件仍为 `AI_TOKEN`，但不单独成为 ExecutionEvent（仅流式透传，不入库）
- 上下文装配/记忆匹配等阶段事件改为 `ExecutionEvent`
- `responseContent` 截断至 500 字符（完整内容在 `TaskDetail.returnParams` 中）
- 按 `turnId` + `sequenceNo` 排序，构成该轮 AI 回复的 **内嵌执行轨迹**

#### MemoryEvidence（记忆证据）

记忆提炼的数据来源。每当一轮对话完成后：

1. 收集该轮所有 `ExecutionEvent`（原子命令调用链+结果）
2. 结合 `reasoningSummary`
3. 构建 `MemoryEvidence`，关联到 `agent_memory_version`

### 4.3 现有模型变更（最小侵入）

| 模型                       | 变更                                                                                                 |
|----------------------------|------------------------------------------------------------------------------------------------------|
| `AgentChatSession`         | **新增** `createUserId`（创建者用户ID），用于归属校验                                                |
| `AgentChatMessage`         | **新增** `turnId`，关联到 `ChatTurn`；**新增** `executionEvents` 非持久化字段（Response DTO 中聚合） |
| `CommandDispatchRequest`   | `userId` 字段改为 **强制填充**（由 `DefaultAgentChatService` 从 LoginUserUtils 获取）                |
| `AgentContext`             | **新增** `userId`、`executorId`（当前执行器）、`clientId`（当前客户端）                              |
| `AgentChatMessageResponse` | **新增** `turnId`、`executionEvents: ExecutionEventDto[]`、`reasoningSummary`                        |

---

## 5. 可信上下文注入契约

### 5.1 字段来源、脱敏与授权矩阵

```
┌────────────────┬──────────────────────┬──────────────────┬────────────────────┬──────────────────┐
│ 字段            │ 来源                  │ 注入时点          │ 脱敏要求            │ 授权校验          │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ userId          │ LoginUserUtils       │ 建会话 / 发消息   │ 无需脱敏            │ 服务端强制获取    │
│                │ .getUserTemporary()   │                  │ (AI 可见)           │ 客户端不可传      │
│                │ .getUserId()          │                  │                    │                  │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ agentId         │ session.agentId      │ 发消息            │ 无需脱敏            │ 校验 session      │
│                │ (DB 已存)             │                  │                    │ 归属 + 启用状态   │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ clientId        │ 前端可选传入          │ 发消息            │ 无需脱敏            │ 校验 client       │
│                │ 或服务端自动匹配        │                  │                    │ 归属当前用户      │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ executorId      │ 由 clientId 推导      │ 发消息            │ 无需脱敏            │ agent_client      │
│                │ (agent_client         │ (上下文装配时)    │                    │ 表关联校验        │
│                │  .executor_id)        │                  │                    │                  │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ clientIp        │ HttpServletRequest   │ 发消息            │ debug 级别日志      │ 审计日志专用      │
│                │ (NEW, 可选)           │                  │ 不传给 AI           │                  │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ 历史消息         │ agent_chat_message   │ 发消息            │ 敏感内容已在        │ session 归属校验  │
│                │ (DB)                 │ (AI 上下文装配)   │ 入库前处理          │                  │
├────────────────┼──────────────────────┼──────────────────┼────────────────────┼──────────────────┤
│ sessionSummary  │ Redis (治理后)        │ 发消息            │ 不包含原始思维链    │ session 归属校验  │
│                │                      │ (AI 上下文装配)   │                    │                  │
└────────────────┴──────────────────────┴──────────────────┴────────────────────┴──────────────────┘
```

### 5.2 三个关键时点的上下文注入时序

```
时点 1: 建会话 (createSession)
  ┌──────────┐          ┌──────────────┐          ┌─────────┐
  │ 前端      │  POST    │ Controller   │  create  │ DB      │
  │ agentId  │─────────►│              │─────────►│         │
  │          │          │ 获取 userId   │          │ INSERT  │
  │          │          │ LoginUserUtils│          │ agent_  │
  │          │          │              │          │ chat_   │
  │          │          │              │          │ session │
  │          │          │ set createUserId          │         │
  └──────────┘          └──────────────┘          └─────────┘
  
  注入字段: userId → session.createUserId

时点 2: 发消息 (sendStream)
  ┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌─────────┐
  │ 前端      │ POST│ Controller   │     │ ChatTurn    │     │ AI      │
  │ sessionId│────►│              │────►│ Service(NEW)│────►│ Client  │
  │ content  │     │ 获取 userId   │     │             │     │         │
  │ clientId?│     │ LoginUserUtils│     │ 加载历史消息 │     │ prompt: │
  │          │     │              │     │ 装配上下文   │     │  system │
  │          │     │              │     │ token 裁剪   │     │  + 历史 │
  │          │     │              │     │ 注入可信字段 │     │  + 当前 │
  └──────────┘     └──────────────┘     └─────────────┘     └─────────┘
  
  注入字段: userId, agentId, clientId, executorId, 历史消息窗口, sessionSummary

时点 3: 执行命令 (executeAtomicCommand)
  ┌─────────────┐     ┌──────────────┐     ┌─────────────┐
  │ AI 决策      │     │ CommandDispatch│   │ WebSocket   │
  │ 调用 tool    │────►│ Service       │───►│ → Executor  │
  │             │     │              │     │             │
  │             │     │ 透传 clientId │     │ COMMAND_    │
  │             │     │ taskId       │     │ BATCH {     │
  │             │     │              │     │   clientId, │
  │             │     │              │     │   taskId,   │
  │             │     │              │     │   commands  │
  │             │     │              │     │ }           │
  └─────────────┘     └──────────────┘     └─────────────┘
  
  注入字段: clientId (来自前端选择或自动匹配), taskId (来自任务创建)
```

### 5.3 防客户端伪造机制

| 字段         | 防伪造策略                                                                    |
|--------------|-------------------------------------------------------------------------------|
| `userId`     | 服务端 `LoginUserUtils` 强制获取，忽略客户端传入的任何 userId                 |
| `agentId`    | 从 `session.agentId`（DB）获取，忽略客户端传入的 agentId                      |
| `clientId`   | 客户端可传，但服务端必须校验 `agent_client` 表：状态=ACTIVE 且归属当前 userId |
| `executorId` | 由 `clientId → agent_client.executor_id` 推导，客户端不可直接传               |
| 历史消息     | 服务端从 DB 加载，客户端不可传                                                |

---

## 6. 会话生命周期与上下文装配

### 6.1 会话状态机

```
                    ┌─────────┐
        createSession│         │
    ───────────────►│  ACTIVE  │◄──────────────┐
                    │         │  deleteSession │
                    └────┬────┘  或超时自动     │
                         │        归档          │
                         │                     │
                    sendStream                 │
                         │                     │
                         ▼                     │
                    ┌─────────┐               │
                    │ RUNNING │───────────────┘
                    │         │  正常完成
                    └─────────┘
                         │
                    sendStream
                    (并发被拒)
                         │
                         ▼
                    返回 409 Conflict
                    "该会话正在处理中，请等待 AI 回复完成"
```

### 6.2 会话复用策略

**原则：前端创建一次会话，后续所有消息复用同一 sessionId。**

| 场景                   | 行为                                                                                  |
|------------------------|---------------------------------------------------------------------------------------|
| 用户选中已有会话发消息 | 复用 sessionId，不创建新会话                                                          |
| 用户点击"新建对话"     | 创建新 sessionId，切换到新会话                                                        |
| 页面刷新 / F5          | 从 `localStorage` 或 URL 恢复 sessionId（待实现），或从服务端 `findSessions` 加载列表 |
| 长时间未操作后恢复     | 加载历史消息（已有分页机制），恢复最近 N 轮上下文                                     |

### 6.3 上下文装配流程（ChatTurnService）

```
ChatTurnService.startTurn(request) → ChatTurn 上下文装配

输入：sessionId, userId, clientId?, modelId?
输出：ChatTurnContext (传递给 CommandDispatchService)

步骤：

1. 锁定会话
   - agentChatSessionView.findByIdForUpdate(sessionId)
   - 校验 status=ON, agentDefinition 启用
   
2. 加载历史消息窗口 (token 裁剪)
   - agentChatMessageView.findAllBySessionId(sessionId)
   - 按 turnId 分组为轮次列表
   - 从最新轮次向前累积，直到 token 估算值接近上限
   - 裁剪策略：
     a. 优先保留最近 N 轮完整对话（默认 N=5）
     b. 更早的轮次仅保留 reasoningSummary（压缩摘要）
     c. 最旧的轮次在超出上限时丢弃
   - Token 估算：字符数 / 2.5（中英文混合近似）
   - 默认上限：由模型 context window 决定，取 model.maxTokens * 0.6

3. 加载 Redis Session Summary
   - agentSessionService.findSummary(sessionId)
   - 该摘要由上一轮完成后异步治理生成

4. 解析 clientId 和 executorId
   - 若 clientId 已传：校验 agent_client 归属
   - 若 clientId 未传：查询当前用户下唯一 ACTIVE 客户端
     （多个在线时返回错误提示前端选择）

5. 组装 AgentContext
   - agentContextAssembler.assemble() 原有逻辑不变
   - 追加注入：userId, clientId, executorId
   - 追加注入：历史消息窗口（结构化 ChatML 格式）

6. 构建 System Prompt（增强）
   原有：definition.systemPrompt
   增强：
   """
   {systemPrompt}
   
   ## 当前上下文
   - 当前用户: {userId}
   - 当前客户端: {clientId} (执行器类型: {executorId})
   - 当前时间: {now}
   
   ## 对话历史摘要
   {sessionSummary}
   
   ## 最近的对话
   {formattedHistory}
   """
```

### 6.4 Token 裁剪策略（详细）

```
裁剪参数:
  MAX_CONTEXT_TOKENS: 模型最大上下文窗口 * 0.6
  FULL_TURN_WINDOW: 最近保留完整内容的轮次数 (默认 5)
  SUMMARY_EARLIER_TURNS: 更早轮次仅保留 reasoningSummary (最多 10 轮摘要)

裁剪算法:
  let tokens = 0
  let turns = [] // 从新到旧
  
  // System Prompt token 预估
  tokens += estimateTokens(systemPrompt)
  
  for turn in reversed(allTurns):
      if turnIndex < FULL_TURN_WINDOW:
          // 完整保留
          turnTokens = estimateTokens(turn.userMessage + turn.assistantMessage)
      else:
          // 仅保留 reasoningSummary
          turnTokens = estimateTokens(turn.reasoningSummary)
      
      if tokens + turnTokens > MAX_CONTEXT_TOKENS:
          break
      
      turns.unshift(turn)
      tokens += turnTokens
```

### 6.5 会话级对话摘要（Session Summary）治理

**当前问题：** Redis 中的 summary 每次用 AI 回复全文覆盖，既不是摘要也不是增量。

**改进方案：**

```
每轮对话完成后 (异步, 不阻塞 SSE):

1. 收集本轮的 ExecutionEvent 原子命令调用链（精简版）
2. 将本轮 userMessage 摘要 + assistantMessage 摘要 + 关键原子命令结果
   → 调用轻量模型（或复用主模型但 temperature=0）生成结构化摘要
3. 摘要格式：
   {
     "turnNumber": 5,
     "userIntent": "用户想创建一个新的客户端实例",
     "aiActions": [
       "查询了执行器类型 exec-001 的详情",
       "创建了名为 test-client 的客户端实例"
     ],
     "outcome": "成功创建客户端实例，ID=xxx",
     "timestamp": "2026-07-22T10:30:00Z"
   }
4. 追加到 Redis List (而非覆盖):
   - RPUSH simple-ai:agent:session:summary-list:{sessionId}
   - LTRIM 保留最近 20 轮摘要
5. 构建 AI 上下文时，从 List 读取并拼接
```

---

## 7. 执行轨迹与记忆提炼

### 7.1 执行轨迹模型（替代当前独立 Timeline）

**核心变化：** 轨迹不再独立展示，而是嵌套在每条 AI 回复消息内，可折叠。

```
┌──────────────────────────────────────────────────────────┐
│  AI 回复                                                  │
│                                                          │
│  根据您的需求，我已为您创建了一个新的客户端实例...           │
│                                                          │
│  ┌─ 执行详情 (可折叠) ─────────────────────────────────┐  │
│  │  ▶ 查询执行器 exec-001                        0.3s  │  │
│  │  ▶ 创建客户端实例 test-client                  0.5s  │  │
│  │  ● AI 生成回复内容                              2.1s  │  │
│  │  模型: 阿里云 · qwen-plus                           │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  09:30  ·  阿里云 · qwen-plus                             │
└──────────────────────────────────────────────────────────┘
```

### 7.2 ExecutionEvent 数据流

```
sendStream 调用开始
  │
  ├─► ChatTurn 创建 (turnId, turnNumber)
  │
  ├─► 上下文装配开始
  │     └─► ExecutionEvent(eventType=CONTEXT_ASSEMBLING, turnId)
  │     └─► ExecutionEvent(eventType=CONTEXT_ASSEMBLED, turnId)
  │
  ├─► 记忆匹配
  │     └─► ExecutionEvent(eventType=MEMORY_MATCHING, turnId)
  │     └─► ExecutionEvent(eventType=MEMORY_MATCHED/MISSED, turnId)
  │
  ├─► 原子命令执行 (每个命令)
  │     └─► ExecutionEvent(eventType=ATOMIC_COMMAND_START, turnId,
  │                         commandName, commandContent, sequenceNo)
  │     └─► ExecutionEvent(eventType=ATOMIC_COMMAND_COMPLETE, turnId,
  │                         responseContent(截断), duration, sequenceNo)
  │
  ├─► AI Token 流式 (不入 ExecutionEvent，仅 SSE 推送)
  │     └─► SSE: AI_TOKEN { payload: "token_text" }
  │
  ├─► 轮次完成
  │     └─► ExecutionEvent(eventType=TURN_COMPLETED, turnId)
  │     └─► ChatTurn.reasoningSummary = 提炼摘要
  │
  └─► 记忆沉淀触发 (异步)
        └─► MemoryEvidence(turnId, evidenceType, evidenceContent)
```

### 7.3 SSE 事件到 ExecutionEvent 的映射

| 当前 SSE eventType                                  | 是否进入 ExecutionEvent                   | 是否 SSE 推送给前端 |
|-----------------------------------------------------|-------------------------------------------|---------------------|
| `TASK_CREATED`                                      | 否（内部事件）                            | 否                  |
| `CONTEXT_ASSEMBLING`                                | 是                                        | 是                  |
| `CONTEXT_ASSEMBLED`                                 | 是                                        | 是                  |
| `RULE_LOADED` / `SKILL_LOADED` / `SUB_AGENT_LOADED` | 是（合并为 CONTEXT_ASSEMBLED 的 payload） | 是                  |
| `MEMORY_MATCHING`                                   | 是                                        | 是                  |
| `MEMORY_MATCHED` / `MEMORY_MISSED`                  | 是                                        | 是                  |
| `STEP_STARTED` (原子命令)                           | 是 (ATOMIC_COMMAND_START)                 | 是                  |
| `STEP_COMPLETED` (原子命令)                         | 是 (ATOMIC_COMMAND_COMPLETE)              | 是                  |
| `AI_STARTED`                                        | 是                                        | 是                  |
| `AI_TOKEN`                                          | **否**                                    | 是（流式消息）      |
| `AI_COMPLETED`                                      | 是                                        | 是                  |
| `TASK_COMPLETED`                                    | 是 (TURN_COMPLETED)                       | 是                  |
| `TASK_FAILED`                                       | 是                                        | 是                  |
| `SUB_AGENT_STARTED` / `COMPLETED`                   | 是                                        | 是                  |
| `MESSAGE_ACCEPTED`                                  | 否（聊天消息层）                          | 是                  |
| `MESSAGE_COMPLETED` / `CHAT_FAILED`                 | 否（聊天消息层）                          | 是                  |

### 7.4 记忆提炼流程

```
每轮对话 TURN_COMPLETED 后 (异步触发, EventBus):

┌────────────────────────────────────────────────────────────────┐
│  AgentMemoryDistiller.distill(turnId)                          │
│                                                                │
│  1. 加载本轮所有 ExecutionEvent (按 turnId)                      │
│     - 提取原子命令调用链 (commandName → result, duration)        │
│     - 提取 AI 模型信息 (provider/model)                         │
│                                                                │
│  2. 判断是否适合沉淀为记忆:                                      │
│     a. 是否执行成功?                                            │
│     b. 是否包含 ≥1 个原子命令?                                   │
│     c. 是否匹配已有记忆? (避免重复)                              │
│     d. 执行链是否可复用?                                        │
│                                                                │
│  3. 若适合沉淀:                                                 │
│     - 提炼最短执行链 (去掉中间步骤冗余)                           │
│     - 构建 agent_memory (若不存在)                              │
│     - 构建 agent_memory_detail (步骤链)                         │
│     - 创建 agent_memory_version (DRAFT 状态)                   │
│     - 创建 MemoryEvidence (关联 turnId 和 versionId)           │
│                                                                │
│  4. 更新 Redis Session Summary (异步)                           │
└────────────────────────────────────────────────────────────────┘
```

**注意：** 此流程是对现有 [`triggerMemoryPrecipitation()`](src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java:1499) TODO 桩的具体实现方案。

### 7.5 推理摘要治理（Reasoning Summary）

**严格禁止存储或传输模型原始思维链 (CoT)。**

`reasoningSummary` 必须是 **受控的结构化摘要**，由服务端在 AI 调用完成后提炼：

```json
{
	"intent": "用户想创建一个新的客户端实例",
	"approach": "先查询执行器类型，再创建客户端实例",
	"actions": [
		{
			"tool": "queryExecutor",
			"target": "exec-001",
			"result": "success"
		},
		{
			"tool": "createClient",
			"target": "test-client",
			"result": "success"
		}
	],
	"outcome": "success",
	"modelUsed": "qwen-plus"
}
```

**提炼方式（二选一，待决策）：**

- 方案 A：基于 ExecutionEvent 自动化模板填充（确定性，无需额外 AI 调用）
- 方案 B：调用轻量模型做摘要（需额外成本，但语义更准确）

---

## 8. SSE 协议与事件契约

### 8.1 增强版 SSE 事件类型

```
现有事件 (保留):
  MESSAGE_ACCEPTED      消息已接收，前端可停止 optimistic loading
  AI_TOKEN              AI 流式 token（payload 为文本片段）
  MESSAGE_COMPLETED     AI 最终回复已保存（payload 为完整回复，completed=true）
  CHAT_FAILED           AI 对话失败（failureReason 含原因）
  (调度内部事件继续通过同一 SSE 通道推送)

新增事件:
  TURN_STARTED          轮次开始 { turnId, turnNumber }
  REASONING_UPDATED     推理摘要更新 { turnId, summary }
  EXECUTION_EVENT       执行事件 { turnId, eventType, stepName, commandName, 
                                   responseContent(截断), duration, sequenceNo }
  SESSION_SUMMARY_UPDATED  会话摘要已更新 { sessionId }
```

### 8.2 前端 SSE 事件路由

```
handleProgress(event):
  switch event.eventType:
    case "AI_TOKEN":
      → appendAssistantToken(messages, event)  // 追加到流式消息
    case "MESSAGE_COMPLETED":
      → replaceFinalMessage(messages, event)    // 替换流式消息为最终消息
    case "CHAT_FAILED":
      → replaceFinalMessage(messages, event)    // 显示错误消息
    case "MESSAGE_ACCEPTED":
      → 确认用户消息已被持久化
    case "TURN_STARTED":
      → 创建新的折叠轨迹区域（关联到当前 AI 回复）
    case "EXECUTION_EVENT":
      → 追加到当前 AI 回复的折叠轨迹中
    case "REASONING_UPDATED":
      → 更新当前 AI 回复的推理摘要
    default:
      // 非消息事件 → 执行轨迹（兼容现有逻辑）
      → appendExecutionEvent(turnEvents, event)
```

---

## 9. 前端交互方案

### 9.1 页面布局变更

**现状：** 三栏布局（会话列表 | 对话消息 | 执行轨迹 Timeline）

**目标：** 两栏布局（会话列表 | 对话消息（含内嵌轨迹））

```
┌────────────┬─────────────────────────────────────────────────────┐
│  会话列表   │  对话消息                                             │
│            │                                                     │
│ ┌────────┐ │  ┌─────────────────────────────────────────────┐   │
│ │ 会话1   │ │  │ 我：帮我创建一个客户端                          │   │
│ │ 会话2 ◀ │ │  │                                             │   │
│ │ 会话3   │ │  │ AI：好的，已为您创建客户端 test-client           │   │
│ │         │ │  │                                             │   │
│ │ [+新建] │ │  │  ┌ 执行详情 ▸ ─────────────────────────┐    │   │
│ └────────┘ │  │  │ (折叠中，点击展开)                     │    │   │
│            │  │  └────────────────────────────────────┘    │   │
│            │  │  阿里云 · qwen-plus  ·  09:30               │   │
│            │  └─────────────────────────────────────────────┘   │
│            │                                                     │
│            │  ┌─────────────────────────────────────────────┐   │
│            │  │ 输入框                                [发送] │   │
│            │  └─────────────────────────────────────────────┘   │
└────────────┴─────────────────────────────────────────────────────┘
```

### 9.2 AI 回复消息结构（React 组件树）

```
<AIMessageBubble>
  <RestrictedMarkdownComponent content={stripProtocolJson(message.content)} />
  
  <CollapsibleExecutionTrace>
    <ExecutionStep icon="search" name="查询执行器 exec-001" duration="0.3s" />
    <ExecutionStep icon="plus" name="创建客户端 test-client" duration="0.5s" />
    <ExecutionStep icon="robot" name="AI 生成回复" duration="2.1s" />
    <ModelTag providerName="阿里云" modelCode="qwen-plus" />
  </CollapsibleExecutionTrace>
  
  <MessageMeta>
    <Timestamp time={message.createTime} />
    <ModelBadge providerName={message.providerName} modelCode={message.modelCode} />
    <ReasoningSummaryBubble summary={turn.reasoningSummary} />  // 可选悬浮展示
  </MessageMeta>
</AIMessageBubble>
```

### 9.3 状态管理变更

```typescript
// 从 AgentChatPage 状态中移除独立的 trajectories 和 events
// 改为在消息中内嵌执行事件

interface AgentChatMessageDto {
    // ... 现有字段
    turnId: string;                              // NEW
    executionEvents: AgentChatExecutionEventDto[]; // NEW (该消息关联的执行事件)
    reasoningSummary: ReasoningSummaryDto | null;  // NEW
}

interface AgentChatExecutionEventDto {
    id: string;
    turnId: string;
    eventType: "ATOMIC_COMMAND_START" | "ATOMIC_COMMAND_COMPLETE" | "ATOMIC_COMMAND_FAILED";
    stepName: string;
    commandName: string;
    responseContent: string;  // 截断版本
    durationMs: number;
    sequenceNo: number;
}
```

### 9.4 折叠轨迹交互

- 默认折叠，显示"执行详情 ▸"
- 点击展开，显示按时间的原子命令调用链
- 每个原子命令显示：图标 + 名称 + 耗时
- 成功的命令绿色图标，失败的红色图标，进行中的旋转动画
- 展开后可通过点击外部区域或再次点击折叠

---

## 10. 事务 / 幂等 / 并发 / 断线重连 / 失败恢复

### 10.1 并发控制

**问题：** 用户快速连续发多条消息可能导致同一 session 的状态不一致。

**方案：会话级分布式锁 + 乐观序号**

```java
// DefaultAgentChatService.sendStream() 开头
String lockKey = "simple-ai:agent:chat:lock:" + sessionId;

boolean locked = lockService.tryLock(lockKey, 30, TimeUnit.SECONDS);
AssertUtils.

isTrue(locked, "该会话正在处理中，请等待 AI 回复完成");

try{
                // ... 原有 sendStream 逻辑
                }finally{
                lockService.

unlock(lockKey);
}
```

或使用数据库行锁（当前已有 `SELECT ... FOR UPDATE`），但需在 `sendStream` 最外层加锁而非仅在 `saveUserMessage` 内加锁。

**推荐方案：** 使用 [`LockService`](skills/simple-common-core) (simple-common-core 提供的分布式锁)，在 `sendStream` 入口处加锁，覆盖整个调用链。

### 10.2 消息幂等

**场景：** 客户端重试 sendStream 可能导致同一消息重复处理。

**方案：** 客户端生成 `idempotencyKey`（UUID），在 `saveUserMessage` 前做去重检查。

```java
// SendAgentChatMessageRequest 新增字段
private String idempotencyKey; // 客户端生成的幂等键

// saveUserMessage 开头
if(idempotencyKey !=null){

// 检查 Redis: SETNX simple-ai:agent:chat:idempotent:{idempotencyKey} 1 EX 300
Boolean isNew = stringRedisTemplate.opsForValue().setIfAbsent("simple-ai:agent:chat:idempotent:" + idempotencyKey, "1", Duration.ofMinutes(5));
    if(!isNew){

// 重复请求，返回已有结果（通过 SSE 重放历史消息事件）
replayExistingTurn(sessionId, idempotencyKey, eventConsumer);
        return;
                        }
                        }
```

### 10.3 序号连续性

当前 `nextSequenceNo` = `MAX(sequence_no) + 1`，在并发场景存在竞态：

- `saveUserMessage` 和 `saveFinalMessage` 分别获取序号，中间无锁保护

**改进：** 在 `sendStream` 最外层加分布式锁后，序号竞态自然消除。

### 10.4 断线重连

**场景：** SSE 连接中断，AI 仍在后台执行。

**当前行为：**

- Controller 的 `emitter.onTimeout()` → `completeWithError`
- 前端 `signal.aborted` → 显示"已停止等待聊天响应"
- 后端 Task/TaskDetail **已落库**（因为调度在独立事务中运行）

**改进方案：**

```
1. 前端检测 SSE 断开
   - 如果是 abort（用户主动取消）→ 不重连
   - 如果是网络断开 → 显示"连接中断，正在重连..."

2. 前端重连机制
   - 指数退避：1s → 2s → 4s → 8s → max 30s
   - 重连时携带 sessionId + turnId（如果有）
   
3. 服务端重连处理
   - 提供 GET /sys/agent-chat/turn/{turnId}/status 查询轮次状态
   - 如果 TURN_COMPLETED → 推送最终消息和事件
   - 如果仍在 RUNNING → 恢复 SSE 推送（从已保存的 ExecutionEvent 继续）
```

### 10.5 失败恢复

| 失败点                     | 恢复策略                                                                                |
|----------------------------|-----------------------------------------------------------------------------------------|
| 用户消息保存失败           | 事务回滚，返回错误，前端保留输入内容                                                    |
| AI 调用失败                | Task 标记 FAILED，TaskDetail 记录失败原因，SSE 推送 CHAT_FAILED，保存 SYSTEM_ERROR 消息 |
| AI 回复保存失败            | SSE 已推送最终内容（前端已展示），异步重试保存（最多3次），告警                         |
| ExecutionEvent 落库失败    | 不影响主流程，异步补偿（从 TaskDetail 反查重建）                                        |
| Redis Session 摘要写入失败 | 不影响主流程，下次对话时从 DB 消息重建摘要                                              |
| 记忆沉淀失败               | 不影响主流程，下次类似命令触发时重试                                                    |

---

## 11. 迁移策略

### 11.1 数据兼容

**原则：** 新增表 + 扩展字段，不修改现有表结构（除非必要）。

| 变更                                | 类型            | 说明                        |
|-------------------------------------|-----------------|-----------------------------|
| `agent_chat_session.create_user_id` | ALTER TABLE ADD | 新建会话填充，历史会话 NULL |
| `agent_chat_message.turn_id`        | ALTER TABLE ADD | 历史消息 NULL，新消息填充   |
| `chat_turn` 表                      | CREATE TABLE    | 全新表                      |
| `execution_event` 表                | CREATE TABLE    | 全新表                      |
| `memory_evidence` 表                | CREATE TABLE    | 全新表                      |

### 11.2 接口兼容

| 接口                                           | 兼容策略                                                                           |
|------------------------------------------------|------------------------------------------------------------------------------------|
| `POST /sys/agent-chat/send-stream`             | 向后兼容。不传 `idempotencyKey` 时跳过幂等检查                                     |
| `GET /sys/agent-chat/message-list/{sessionId}` | 响应体新增 `turnId`、`executionEvents`、`reasoningSummary` 字段。前端可选择性使用  |
| `GET /sys/agent-chat/trajectory/{sessionId}`   | **保留但标记 deprecated**。新前端不再调用，改为从消息响应的 `executionEvents` 获取 |
| `SendAgentChatMessageRequest`                  | 新增可选字段 `idempotencyKey`                                                      |

### 11.3 前端迁移

```
阶段 1: 前端同时兼容新旧响应格式
  - 检查 message.executionEvents 是否存在
  - 存在 → 使用内嵌轨迹渲染
  - 不存在 → 回退到独立 Timeline 渲染

阶段 2: 后端全量生成 executionEvents 后
  - 移除独立 Timeline 组件
  - 移除 findTrajectory 调用
  - 废弃 GET /sys/agent-chat/trajectory 接口
```

---

## 12. 分阶段实施计划与验收标准

### 阶段 0：基础设施（DB 变更 + 模型）

| #   | 任务                                                | 验收标准                        |
|-----|-----------------------------------------------------|---------------------------------|
| 0.1 | 创建 `chat_turn` DDL                                | 表结构符合领域模型定义          |
| 0.2 | 创建 `execution_event` DDL                          | 含 turnId 索引、eventType 索引  |
| 0.3 | 创建 `memory_evidence` DDL                          | 含 turnId、memoryVersionId 索引 |
| 0.4 | `ALTER TABLE agent_chat_session ADD create_user_id` | DDL 执行成功                    |
| 0.5 | `ALTER TABLE agent_chat_message ADD turn_id`        | DDL 执行成功，历史数据兼容      |
| 0.6 | 创建 Entity + View + Repository + Mapper XML        | `mvn clean compile` 通过        |

### 阶段 1：可信上下文注入

| #   | 任务                                                                                      | 验收标准                                                     |
|-----|-------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| 1.1 | `DefaultAgentChatService` 在 `createSession` 中填充 `createUserId`                        | `LoginUserUtils.getUserTemporary().getUserId()` 写入 session |
| 1.2 | `DefaultAgentChatService.dispatchAgent()` 填充 `CommandDispatchRequest.userId`            | 从 LoginUserUtils 获取并赋值                                 |
| 1.3 | `AgentContext` 新增 `userId`、`clientId`、`executorId` 字段，`AgentContextAssembler` 填充 | read_file 验证字段存在且赋值                                 |
| 1.4 | `resolveClientIdIfAbsent()` 实现自动匹配                                                  | 查询当前用户下唯一 ACTIVE 客户端，多个时抛异常               |
| 1.5 | `SpringAiAgentAiClient.buildUserContent()` 注入结构化上下文                               | System Prompt 包含 userId/clientId/executorId/history        |

### 阶段 2：会话复用与多轮上下文

| #   | 任务                                                                       | 验收标准                                     |
|-----|----------------------------------------------------------------------------|----------------------------------------------|
| 2.1 | `ChatTurnService` 实现：创建轮次、加载历史消息窗口、装配 ChatML 格式上下文 | 单元测试覆盖                                 |
| 2.2 | Token 裁剪算法实现                                                         | 按字符数估算，保留最近5轮完整内容 + 更早摘要 |
| 2.3 | Session Summary 治理：从覆盖改为 List 追加 + 轻量摘要生成                  | Redis 中存储最近20轮摘要                     |
| 2.4 | `DefaultChatAgentService.sendStream()` 集成 ChatTurnService                | 发消息时自动创建 turn，注入上下文            |

### 阶段 3：执行轨迹重构

| #   | 任务                                                                                  | 验收标准                             |
|-----|---------------------------------------------------------------------------------------|--------------------------------------|
| 3.1 | `ExecutionEvent` 实体 + View + Repository                                             | `mvn clean compile` 通过             |
| 3.2 | `ExecutionEventBus`：事件发布/订阅，在 `DefaultCommandDispatchService` 各阶段发布事件 | 每个原子命令执行前后发布事件         |
| 3.3 | `AgentChatMessageResponse` 扩展 `executionEvents` 字段                                | 查询消息时 JOIN 关联 execution_event |
| 3.4 | 前端 `AIMessageBubble` 组件：内嵌折叠执行轨迹                                         | 展开/折叠交互正常，原子命令按序展示  |

### 阶段 4：记忆提炼

| #   | 任务                                                                                | 验收标准                                       |
|-----|-------------------------------------------------------------------------------------|------------------------------------------------|
| 4.1 | `AgentMemoryDistiller` 实现：收集 ExecutionEvent，判断沉淀条件，创建 MemoryEvidence | 单元测试覆盖                                   |
| 4.2 | 实现 `triggerMemoryPrecipitation()` TODO 桩                                         | 替代现有空实现                                 |
| 4.3 | 记忆版本管理集成（关联到现有 `agent_memory_version` 流程）                          | 端到端测试：一轮对话 → 自动生成 DRAFT 记忆版本 |

### 阶段 5：断线重连与容错

| #   | 任务                                            | 验收标准                      |
|-----|-------------------------------------------------|-------------------------------|
| 5.1 | 前端 SSE 断线检测 + 指数退避重连                | 网络断开后自动重连            |
| 5.2 | `GET /sys/agent-chat/turn/{turnId}/status` 接口 | 返回轮次状态                  |
| 5.3 | 消息幂等（idempotencyKey）                      | 重复请求不产生重复消息        |
| 5.4 | 会话级分布式锁                                  | 同一 session 并发请求返回 409 |

### 阶段 6：清理与文档

| #   | 任务                                                                 | 验收标准                |
|-----|----------------------------------------------------------------------|-------------------------|
| 6.1 | 废弃 `GET /sys/agent-chat/trajectory/{sessionId}`（标记 deprecated） | Swagger 标注 deprecated |
| 6.2 | 前端移除独立 Timeline 面板                                           | 两栏布局正常            |
| 6.3 | 更新 API 文档                                                        | Swagger 文档完整        |

---

## 13. 风险与待澄清项

### 13.1 已知风险

| 风险                                | 影响                    | 缓解措施                                                   |
|-------------------------------------|-------------------------|------------------------------------------------------------|
| Token 裁剪可能丢失关键上下文        | AI 回复质量下降         | 保留 reasoningSummary 作为压缩上下文；可配置裁剪参数       |
| Session Summary 异步治理失败        | 上下文退化（但不丢失）  | 降级：下次对话时从 DB 消息历史重建                         |
| SSE 重连后事件重复推送              | 前端重复渲染            | 客户端按 turnId + sequenceNo 去重                          |
| DDL 变更与现有数据兼容              | 历史数据 migration 失败 | 所有新字段默认 NULL，新逻辑对 NULL 做防御性处理            |
| `reasoningSummary` 提炼可能不够准确 | 记忆沉淀质量下降        | 采用方案 A（模板填充）作为兜底，方案 B（模型摘要）作为增强 |

### 13.2 待澄清项

| #  | 问题                                                                                                | 需要谁确认    |
|----|-----------------------------------------------------------------------------------------------------|---------------|
| Q1 | `agent_chat_session` 是否需要 `tenantId` 多租户字段？当前实体中无此字段                             | 架构负责人    |
| Q2 | `agent_chat_message` 历史数据是否需要回填 `turnId`？若不需要，旧消息的 `executionEvents` 如何关联？ | 产品/架构     |
| Q3 | Token 裁剪的默认窗口大小（5轮）和估算参数（2.5 chars/token）是否合适？                              | AI 平台负责人 |
| Q4 | Reasoning Summary 提炼采用方案 A（模板填充）还是方案 B（模型摘要）？                                | 技术负责人    |
| Q5 | SSE 断线重连的最大重试次数和超时时间？                                                              | 前端/产品     |
| Q6 | `createUserId` 是否需要对历史数据回填？                                                             | DBA/架构      |
| Q7 | 现有的 `agent_chat_session` 表 DDL 是否需确认？当前分析仅基于实体类反向推断                         | DBA           |

---

## 附录 A：调用链速查

```
AgentChatPage.handleSend()
  → AgentChatApi.sendStream({ sessionId, content, modelId?, clientId? })
    → fetch POST /sys/agent-chat/send-stream (SSE)
      → AgentChatController.sendStream()
        → taskExecutor.execute(runChatStream)
          → agentChatService.sendStream(request, eventConsumer)
            → DefaultAgentChatService.sendStream()
              → saveUserMessage() [TX: SELECT FOR UPDATE → INSERT message → UPDATE session]
              → publishChatEvent("MESSAGE_ACCEPTED")
              → dispatchAgent() → commandDispatchService.dispatchStream()
                → DefaultCommandDispatchService.executeDispatchInternal()
                  → createRunningTask() → taskView.save(task)
                  → agentContextAssembler.assemble() → AgentContext
                  → agentMemoryMatcher.match() → memories
                  → executeCommand()
                    → executeMemorySteps() 或 executeAiExploration()
                      → agentAiClient.chatStream() → Spring AI + toolCallbacks
                  → saveSessionSummary() → RedisAgentSessionService
              → saveFinalMessage() [TX: SELECT FOR UPDATE → INSERT message → UPDATE session]
              → publishFinalEvent("MESSAGE_COMPLETED"/"CHAT_FAILED")
```

## 附录 B：关键文件索引

| 文件                        | 路径                                                                                | 行数 |
|-----------------------------|-------------------------------------------------------------------------------------|------|
| Controller                  | `src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java`         | 197  |
| Chat Service                | `src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java`        | 640  |
| Chat Service 接口           | `src/main/java/com/simple/ai/common/service/agentChat/AgentChatService.java`        | 83   |
| Dispatch Service            | `src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java`    | 1575 |
| AI Client                   | `src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java`              | 181  |
| AI Client 接口              | `src/main/java/com/simple/ai/common/service/agent/AgentAiClient.java`               | 40   |
| Tool Registry               | `src/main/java/com/simple/ai/service/agent/AgentToolRegistry.java`                  | 320  |
| Session Service (Redis)     | `src/main/java/com/simple/ai/service/session/RedisAgentSessionService.java`         | 118  |
| AgentContext                | `src/main/java/com/simple/ai/common/dto/agent/AgentContext.java`                    | 84   |
| CommandDispatchRequest      | `src/main/java/com/simple/ai/common/dto/command/CommandDispatchRequest.java`        | 69   |
| SendAgentChatMessageRequest | `src/main/java/com/simple/ai/common/dto/agentChat/SendAgentChatMessageRequest.java` | 41   |
| Session Entity              | `src/main/java/com/simple/ai/common/entity/agentChatSession/AgentChatSession.java`  | 63   |
| Message Entity              | `src/main/java/com/simple/ai/common/entity/agentChatMessage/AgentChatMessage.java`  | 121  |
| Task Entity                 | `src/main/java/com/simple/ai/common/entity/task/Task.java`                          | 163  |
| Session Mapper              | `src/main/resources/mapper/AgentChatSessionDao.xml`                                 | 20   |
| Message Mapper              | `src/main/resources/mapper/AgentChatMessageDao.xml`                                 | 42   |
| 前端 Chat 页面              | `web/src/pages/AgentChatPage.tsx`                                                   | 591  |
| 前端 Stream Util            | `web/src/utils/agentChatStreamUtil.ts`                                              | 162  |
| 前端 Chat API               | `web/src/api/agentChatApi.ts`                                                       | 103  |
| 前端 Chat DTO               | `web/src/dto/agentChat/AgentChatDto.ts`                                             | 61   |
