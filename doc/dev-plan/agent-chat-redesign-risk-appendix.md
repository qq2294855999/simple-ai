# Agent Chat Redesign - Risk, Appendix A, Appendix B (sec13)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: Pending

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
