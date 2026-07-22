# Agent Chat Redesign - Session and Trajectory (sec5-sec9)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: Pending

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
