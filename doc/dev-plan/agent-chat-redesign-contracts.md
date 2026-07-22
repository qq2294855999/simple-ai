# Agent Chat Redesign - Migration Strategy (sec11)

> Parent: [agent-chat-session-context-trajectory-redesign.md](./agent-chat-session-context-trajectory-redesign.md)
> Status: Pending

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
