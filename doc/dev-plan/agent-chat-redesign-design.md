# Agent Chat Redesign - Design Analysis (sec1-sec4)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: Pending

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
