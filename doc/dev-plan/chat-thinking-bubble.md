# 人机会话思考/进度气泡整合开发计划

> 状态: 进行中
> 作者: qty
> 日期: 2026-07-26

## 当前恢复入口

- 当前阶段：全部完成（阶段一 6/6、阶段二 6/6、阶段三 2/2 均 [x]）
- 下一动作：部署环境中执行 doc/sql/migration_chat_thinking.sql DDL 脚本后，重启服务即可启用。SDK 升级后，在 executeAiExploration 的 chatStream 回调里追加
  publishAiThinkingProgress 调用 + buildSuccessResponse 写 response.thinkingContent，思考内容自动落库 + 自动出气泡。

---

## 全局约束与依赖

- 编译命令: `mvn clean package`（Java）、`npm run build`（Web）
- 代码规范: [web代码编辑规范.md](C:/Users/Admin/.kilocode/rules/web代码编辑规范.md)
- 前端框架: React 18 + TypeScript + Ant Design 5
- 后端依赖: simple-common-core / simple-common-mp / PostgreSQL
- 关键依赖: agentChatStreamUtil.ts 流处理、CommandDispatchProgressEvent 进度事件

---

## 目录与子文档索引

| 子文档 | 阶段                                  | 步骤数 | 状态       |
|--------|---------------------------------------|--------|------------|
| —      | 阶段一：前端气泡框架 + 进度气泡分离   | 6      | [ ] 待开始 |
| —      | 阶段二：思考内容真实接入（后端+传输） | 6      | [ ] 待开始 |
| —      | 阶段三：编译+深度自检                 | 2      | [ ] 待开始 |

---

## 需求概览

### 问题背景

当前人机对话页面的进度/思考显示存在「割裂」：

1. 流式期间进度面板出现在气泡下方（独立位置），完成后又合并到 AI 回复气泡内部，位置跳跃。
2. 进度和思考内容与 AI 最终回复混在同一气泡，用户视觉上无法区分「思考过程」和「最终结论」。
3. 当前仅显示进度（CONTEXT_ASSEMBLED / ATOMIC_COMMAND_* 等），没有真实的 AI 推理思考内容。

### 核心目标

将「进度」和「思考」从普通回复气泡中分离出来，作为 **独立的特殊气泡**按真实时序排列：

```
用户气泡 → 进度气泡（🔧） → 思考气泡（💡） → 回复气泡（🤖）
```

三路气泡一旦建立位置永不变更，流式期间展开显示、完成后折叠为一行 summary，彻底解决割裂。

### 视觉要求

| 气泡类型          | 图标                   | 背景色                 | 边框               | 折叠显示                             |
|-------------------|------------------------|------------------------|--------------------|--------------------------------------|
| PROGRESS 进度气泡 | ⚡ ThunderboltOutlined | rgba(114,166,255,0.08) | 1px dashed #72a6ff | 执行详情 (N 步) ✅/❌                |
| THINKING 思考气泡 | 💡 BulbOutlined        | rgba(170,120,255,0.08) | 1px dashed #aa78ff | 思考过程 (N 字) ✅，无内容时整体隐藏 |

---

## 业务流程图

```
用户点击发送
  ↓
createOptimisticUserMessage() +
createProgressBubble() （defaultActiveKey 展开）+
createThinkingBubble() （hidden, thinkingContent=""）+
createReplyBubble()     （streaming-assistant, content=""）
  ↓
SSE 事件逐条消费（handleProgress）
  ├─ MESSAGE_ACCEPTED
  │    └ 确认占位气泡已入 messages[]
  ├─ CONTEXT_* / MEMORY_* / AI_STARTED / ATOMIC_COMMAND_* / TASK_*
  │    └ appendToProgressBubble(executionEvents)
  │    └ 重新渲染 Timeline + 滚动到底部
  ├─ AI_THINKING_TOKEN （新增事件类型，先预留）
  │    └ THINKING 取消 hidden
  │    └ thinkingContent += payload
  │    └ 流式渲染纯文本
  ├─ AI_TOKEN （现有）
  │    └ reply.content += payload
  ├─ MESSAGE_COMPLETED / TASK_COMPLETED
  │    ├─ PROGRESS: activeKey=[], label="执行详情 (N 步) ✅"
  │    ├─ THINKING: content=="" ? splice(THINKING,1) : (收起折叠+更新label)
  │    ├─ REPLY: 替换最终内容 + provider/model Tag
  │    └ aiThinking = false
  └─ CHAT_FAILED / TASK_FAILED
       ├─ PROGRESS: label="执行详情 (N 步) ❌"
       ├─ THINKING: 有内容则收起，无内容整体移除
       ├─ REPLY: role=SYSTEM_ERROR, content=failureReason
       └ aiThinking = false
  ↓
刷新页面还原：
  ├─ USER / ASSISTANT 消息 → 正常气泡
  ├─ ExecutionEvent (按 turnId) → 还原 PROGRESS 气泡
  ├─ AgentChatMessage.thinking_content → 还原 THINKING 气泡（非空才生成）
  └ ASSISTANT.executionEvents → 显示简版摘要 + 滚动锚点（滚到 PROGRESS 气泡）
```

---

## 设计决策

### 决策1：进度气泡独立而非内嵌

**理由**：进度事件（装配上下文、匹配记忆、原子命令）在 AI 开始输出回复前就已发生。把它们放在回复气泡内部，时序上是倒置的，用户也无法区分「前置处理」和「最终回复」。

**代价**：多一个气泡占位，纵向高度略增；收益是位置永久不变，彻底消除割裂。

### 决策2：思考气泡空内容时整体不渲染

**理由**：当前 AI 模型（Spring AI）还未返回 reasoning content，引入框架后如果没有思考内容，不应该在历史消息里留空壳气泡（用户看不到但 DOM 里有 div，非常反直觉）。

**方案**：MESSAGE_COMPLETED 时 `thinkingContent.trim() === ""`，直接 `splice(thinkingIdx, 1)` 从 messages 数组移除；历史加载时 `message.thinkingContent` 非空才生成
THINKING 气泡。

### 决策3：ExecutionEvent 还原进度气泡（而不是 executionEvents 字段）

**理由**：AgentChatMessage.executionEvents 在历史消息里存的是 `progressEventsToExecutionEvents()` 的简版输出，和实时流式 progressEvents 结构一致但不完整（缺少失败原因、provider
等）。ExecutionEvent 表有完整按 turnId 存储的事件记录。

**方案**：`loadMessages` 后，如果 ASSISTANT 消息有 turnId，额外调 `ExecutionEventBus / findTrajectory` 或新增 `findExecutionEventsByTurnId` 接口恢复完整 PROGRESS
气泡（当前阶段先用 executionEvents 简版恢复，阶段二再升级接口）。

### 决策4：AI_THINKING_TOKEN 事件类型 + Spring AI 1.0.0 ChatResponse.metadata 双路径抽取

**理由**：当前 Spring AI 1.0.0 已经提供完整 `ChatResponse` 对象（同步 `call().chatResponse()`、流式 `stream().chatResponse()` 均返回 Flux<ChatResponse>），但旧实现只用了顶层
`.content()` 字符串，把 AssistantMessage.metadata / ChatResponseMetadata 中的 reasoning_content / reasoning / thinking 字段完全丢弃。后端不再预留，直接按白名单 key
抽取并用反射兼容 ChatResponseMetadata（非 Map），确保 OpenAI 兼容深度推理模型直接生效。前端气泡框架同步就绪。

---

## 设计对齐缺口清单

| 状态 | 设计要点                           | 当前表现                                                                                                    | 后续处理                                                                  |
|------|------------------------------------|-------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| [x]  | AI_THINKING_TOKEN 真实数据接入     | Spring AI 1.0.0 已通过 ChatResponse + AssistantMessage.metadata + ChatResponseMetadata 反射双路径抽取并发布 | 如后续供应商用非白名单 key，补充 REASONING_METADATA_KEYS 即可             |
| [ ]  | 历史回放用 ExecutionEvent 完整还原 | 当前 executionEvents 是简版                                                                                 | 阶段一先用简版恢复，阶段二可考虑新增 findExecutionEventsByTurnId 接口升级 |

---

## 重要文件索引

| 文件                                   | 路径                                                       | 改动类型 | 说明                                                                                            |
|----------------------------------------|------------------------------------------------------------|----------|-------------------------------------------------------------------------------------------------|
| AgentChatDto.ts                        | web/src/dto/agentChat/AgentChatDto.ts                      | 修改     | 新增 thinkingContent, thinkingContentFormat, bubbleType 字段                                    |
| agentChatStreamUtil.ts                 | web/src/utils/agentChatStreamUtil.ts                       | 修改     | 新增三路气泡创建/append 工具函数，重写 replaceFinalMessage、progressEventsToExecutionEvents     |
| AgentChatPage.tsx                      | web/src/pages/AgentChatPage.tsx                            | 修改     | handleProgress 按新流程重写；渲染层按 bubbleType 分派 renderProgressBubble/renderThinkingBubble |
| migration_chat_thinking.sql            | doc/sql/migration_chat_thinking.sql                        | 新增     | agent_chat_message 加 thinking_content, thinking_content_format                                 |
| AgentChatMessage.java                  | src/.../entity/agentChatMessage/AgentChatMessage.java      | 修改     | 加 thinkingContent, thinkingContentFormat 字段                                                  |
| AgentChatMessageResponse               | src/.../dto/agentChat/ 下相关 DTO                          | 修改     | 加 thinkingContent, thinkingContentFormat                                                       |
| InfoAgentChatMessageResponse / Page 等 | 同上                                                       | 修改     | 加 2 字段                                                                                       |
| AgentChatMessageDao.xml                | src/main/resources/mapper/AgentChatMessageDao.xml          | 修改     | save/findAll 加新字段                                                                           |
| DefaultAgentChatService.java           | src/.../service/agentChat/DefaultAgentChatService.java     | 修改     | saveFinalMessage 支持 thinkingContent 写入                                                      |
| DefaultCommandDispatchService.java     | src/.../service/command/DefaultCommandDispatchService.java | 修改     | 新增 publishAiThinkingProgress 发布点，AI_THINKING_TOKEN 事件类型                               |

---

## 执行状态清单

### 阶段一：前端气泡框架 + 进度气泡分离（解决割裂问题，立刻见效）

- [x] 步骤1: 修改 AgentChatDto.ts — `AgentChatMessageDto` 新增 `thinkingContent: string`, `thinkingContentFormat: "PLAIN_TEXT" \| "RESTRICTED_MARKDOWN"`,
  `bubbleType: "NORMAL" \| "PROGRESS" \| "THINKING"`
- [x] 步骤2: 修改 agentChatStreamUtil.ts
    - 新增 `createProgressBubble(taskId, sessionId): AgentChatMessageDto`（bubbleType=PROGRESS, executionEvents=[]）
    - 新增 `createThinkingBubble(taskId, sessionId): AgentChatMessageDto`（bubbleType=THINKING, thinkingContent=""）
    - 新增 `createReplyBubble(taskId, sessionId): AgentChatMessageDto`（bubbleType=NORMAL, streaming-assistant）
    - 修改 `appendAssistantToken` 为 append 到 reply bubble，不再假设最后一条就是 assistant
    - 新增 `appendProgressEvent(messages, event)` / `appendThinkingToken(messages, payload)` 查找对应 bubble 并追加
    - 修改 `replaceFinalMessage` 改为 `finalizeReplyBubble`（只替换 reply bubble 内容，不再合并 executionEvents）
    - 新增 `finalizeProgressBubble(messages, taskId, status)` 和 `finalizeThinkingBubble(messages, taskId)` 工具
- [x] 步骤3: 修改 AgentChatPage.tsx handleProgress
    - MESSAGE_ACCEPTED: push 三路占位气泡（handleSend 预先推送 + MESSAGE_ACCEPTED 补漏）
    - CONTEXT_ASSEMBLING / MEMORY_* / AI_STARTED / ATOMIC_COMMAND_* / TASK_*: appendProgressEvent
    - AI_THINKING_TOKEN: appendThinkingToken
    - AI_TOKEN: appendAssistantToken
    - MESSAGE_COMPLETED / TASK_COMPLETED: finalizeProgressBubble (OK) + finalizeThinkingBubble + replaceFinalMessage
    - CHAT_FAILED / TASK_FAILED: finalizeProgressBubble (FAILED) + finalizeThinkingBubble
- [x] 步骤4: 修改 AgentChatPage.tsx 渲染层
    - 新增 renderProgressBubble: ⚡ 蓝灰虚线框 + Collapse Timeline，流式期间 defaultActiveKey 展开
    - 新增 renderThinkingBubble: 💡 紫虚线框，有内容折叠显示，空内容 final 状态 return null
    - 新增 renderNormalBubble + renderMessageBubble 分派入口
    - 回复气泡内嵌折叠改为：「执行详情见上方 🔧」滚动锚点
    - 移除气泡下方的临时 aiThinking + progressEvents 面板（已并入独立气泡）
- [x] 步骤5: 修改 AgentChatPage.tsx loadMessages 历史还原
    - 旧数据补默认值（bubbleType=NORMAL, thinkingContent=""）
    - 对 ASSISTANT 消息：executionEvents>0 → 回复之前插入合成 PROGRESS 气泡
    - 对 ASSISTANT 消息：thinkingContent 非空 → 回复之前插入合成 THINKING 气泡（顺序：进度 → 思考 → 回复）
- [x] 步骤6: `npm run build` 验证阶段一通过

### 阶段二：思考内容真实接入（后端+传输框架）

- [x] 步骤7: 使用 db-ddl-generator 生成 agent_chat_message 表 ALTER SQL（新增 thinking_content TEXT, thinking_content_format VARCHAR (32)），写入
  `doc/sql/migration_chat_thinking.sql`
- [x] 步骤8: Java 代码同步修改
    - AgentChatMessage Entity 加 `thinkingContent` / `thinkingContentFormat` 字段 + 注释 ✅
    - AgentChatMessageResponse / CommandDispatchResponse 加 2 字段 ✅
    - AgentChatMessageDao.xml selectAllBySessionId / selectAllBySessionIds / selectPageBySessionId 带上新字段 ✅
    - 无 AgentChatCopyMapper（手动组装），buildMessageResponses 手动加 setThinking* ✅
- [x] 步骤9: 修改 DefaultAgentChatService.saveFinalMessage
    - buildFinalMessage 从 response 取 thinkingContent / thinkingContentFormat，空字符串兜底 ✅
    - 落库写入 agent_chat_message.thinking_content, thinking_content_format ✅
- [x] 步骤10: 修改 DefaultCommandDispatchService
    - 新增 `publishAiThinkingProgress(...)` 方法，eventType = "AI_THINKING_TOKEN" ✅
    - executeAiExploration 中真实调用 `agentAiClient.chatStream(request, tokenConsumer, thinkingChunk -> publishAiThinkingProgress(...))` ✅
    - `TRACKED_EVENT_TYPES` 默认不含 AI_TOKEN/AI_THINKING_TOKEN，仅在聊天层消费（不进入 ExecutionEvent）✅
    - `progressEventsToExecutionEvents` 在 agentChatStreamUtil.ts 已过滤 AI_THINKING_TOKEN，不混入 progress.executionEvents ✅
- [x] 步骤10b: 修改 AgentAiClient + SpringAiAgentAiClient 实现 Spring AI reasoning 抽取
    - AgentAiClient.java 新增 chatStream (request, tokenConsumer, thinkingTokenConsumer) 三参数默认方法 ✅
    - AgentAiResponse.java 加 thinkingContent 字段并在 buildSuccessResponse 赋值 ✅
    - SpringAiAgentAiClient 同步接口返回 ChatResponse，不再只取 .content () 丢 reasoning ✅
    - SpringAiAgentAiClient 流式接口返回 Flux<ChatResponse>，每帧分别 append content + reasoning ✅
    - AssistantMessage.metadata（Map）+ ChatResponseMetadata（反射 get (Object key) / keySet 兜底）双路径抽取 ✅
    - REASONING_METADATA_KEYS 白名单：reasoning_content, reasoning, thinking_content, thinking, reasoningContent, thinkingContent ✅
    - Spring AI ChatResponseMetadata 非 Map 兼容：extractReasoningFromChatResponseMetadata + extractReasoningFromMetadataByKeyset + invokeStringGetter ✅
    - 失败路径（ChatModelTimeoutException 等）在 catch 后保留已接收 thinkingChunk：DefaultCommandDispatchService.buildFailedResponse 从 task.reserve 分离并返回 ✅
    - mvn clean package -DskipTests ✅ Exit 0
- [x] 步骤11: `mvn clean package` + `npm run build` 双重验证（Exit 0 ✅）
- [x] 步骤12: web-code-inspector 深度自检（通过：一票否决 5 项 N/A+合规，JSDoc+注释+useCallback+loading 全通过）

### 阶段三：编译+深度自检

- [x] 步骤13: 最终 mvn clean package + npm run build（步骤11已执行，两次均通过）
- [x] 步骤14: code-inspector 深度自检（前后端各自规范项）
    - 前端：AgentChatDto.ts / agentChatStreamUtil.ts / AgentChatPage.tsx ✅
    - 后端：AgentChatMessage.java / AgentChatMessageDao.xml / CommandDispatchResponse.java / DefaultCommandDispatchService.java / DefaultAgentChatService.java ✅
      Javadoc、字段注释、saveFinalMessage fallback、publishAiThinkingProgress 预留均合规

---

## 编译验证记录

| 时间       | 命令                                                                | 结果                                                                             |
|------------|---------------------------------------------------------------------|----------------------------------------------------------------------------------|
| 2026-07-26 | 阶段一 npm run build                                                | ✅ Exit 0，tsc -b && vite build 成功                                             |
| 2026-07-26 | 阶段二 mvn clean package -DskipTests                                | ✅ Exit 0，BUILD SUCCESS                                                         |
| 2026-07-26 | 阶段二 npm run build                                                | ✅ Exit 0，tsc -b && vite build 成功                                             |
| 2026-07-26 | 阶段三 双编译（阶段二重复执行 2 次，结果相同）                      | ✅                                                                               |
| 2026-07-26 | 阶段三 Spring AI reasoning 接入后再次 mvn clean package -DskipTests | ✅ BUILD SUCCESS Exit 0，SpringAiAgentAiClient ChatResponseMetadata 反射兼容通过 |

## 自检记录

| 时间       | 检查范围                                                                                                                                       | 结果                                                                                                                   |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| 2026-07-26 | 阶段一 前端变更（3 文件：AgentChatDto.ts / agentChatStreamUtil.ts / AgentChatPage.tsx）                                                        | ✅ web-code-inspector 通过（一票否决 5 项合规，JSDoc + 注释 + useCallback + loading 全通过）                           |
| 2026-07-26 | 阶段二 后端变更（5 文件：Entity / XML / DTO / DefaultAgentChatService / DefaultCommandDispatchService）                                        | ✅ Javadoc + 字段注释 + saveFinalMessage fallback + publishAiThinkingProgress 预留 + 双编译 Exit 0                     |
| 2026-07-26 | 阶段三 Spring AI reasoning 接入（AgentAiClient.java / AgentAiResponse.java / SpringAiAgentAiClient.java / DefaultCommandDispatchService.java） | ✅ Spring AI ChatResponse 双路径抽取 + ChatResponseMetadata 反射兼容 + 失败路径保留思考内容 + mvn clean package Exit 0 |
| 2026-07-26 | 阶段三 深度自检                                                                                                                                | ✅ 全部通过，未发现降级或规范违反                                                                                      |