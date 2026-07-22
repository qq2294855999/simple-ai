# Agent Chat Redesign - Concurrency and Idempotency (sec10)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: Pending

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
