# 会话上下文重构开发计划

## 当前恢复入口

- 当前阶段：Bug 修复完成（ToolCallback 异步线程获取 userId）。
- 当前进行中：无。
- 下一步：等待用户验证功能。

## 执行状态清单

- [x] 修改 CreateAgentChatSessionRequest，增加 modelId、clientId 必填字段
- [x] 修改 SendAgentChatMessageRequest，移除 modelId、clientId 字段
- [x] 修改 agent_chat_session 表，增加 model_id、client_id 字段
- [x] 修改 AgentChatSession 实体，增加 modelId、clientId 属性
- [x] 修改 AgentChatSessionResponse，增加 modelId、clientId 属性
- [x] 修改 DefaultAgentChatService.createSession，保存 modelId、clientId
- [x] 修改 DefaultAgentChatService.dispatchAgent，从 session 获取 userId、modelId、clientId
- [x] 创建 AgentSessionContext，提供 ThreadLocal 存储 sessionId
- [x] 修改 DefaultCommandDispatchService.executeAiExploration，设置/清理会话上下文
- [x] 修改 AgentToolRegistry.createClient，从 session 获取 userId
- [x] 修改前端 CreateAgentChatSessionRequestDto，增加 modelId、clientId
- [x] 修改前端 SendAgentChatMessageRequestDto，移除 modelId、clientId
- [x] 修改前端 AgentChatPage，三个参数同一行，创建会话时传递完整参数
- [x] 执行 mvn clean compile
- [x] 执行 npm run build
- [x] 执行 code-inspector 深度自检
- [x] Bug 修复：数据库添加 model_id、client_id 字段
- [x] Bug 修复：buildSessionResponse 补充 modelId、clientId 回显
- [x] Bug 修复：前端 AgentChatSessionDto 增加 modelId、clientId
- [x] Bug 修复：前端历史会话列表回显模型和客户端信息
- [x] Bug 修复：点击历史会话时回显 Select 值
- [x] 执行 npm run build 验证
- [x] Bug 修复：AgentAiRequest 增加 sessionId、userId 字段
- [x] Bug 修复：buildAiRequest 传递 sessionId、userId
- [x] Bug 修复：SpringAiAgentAiClient 使用并发 Map 存储 sessionId→userId
- [x] Bug 修复：AgentToolRegistry 从并发 Map 获取 userId
- [x] 执行 mvn clean compile 验证

## 重点业务流程说明

### 问题根因

```
HTTP请求线程 → LoginUserUtils.set() → Controller → Service → taskExecutor.execute() → AI工具回调
                                                                    ↑
                                                          新线程，ThreadLocal 丢失
```

`LoginUserUtils` 基于 `ThreadLocal` 存储用户信息，但 AI 工具回调（如 `createClient`）是在异步线程中执行的，`ThreadLocal` 已丢失。

### 解决方案

创建会话时保存完整上下文（userId、modelId、clientId）到会话表，发送消息时从会话获取，工具回调通过 sessionId 查会话获取 userId。

### 业务流程图

```
用户选择智能体、模型、客户端
  ↓
□ 创建会话 (POST /session)
  ├── 参数：agentId, modelId, clientId
  ├── 服务端：从 token 解析 userId
  └── 落库：agent_chat_session (agent_id, user_id, model_id, client_id)
  ↓
[sessionId] 返回前端
  ↓
用户发送消息
  ↓
□ 发送消息 (POST /send-stream)
  ├── 参数：sessionId, content
  ├── 服务端：通过 sessionId 查会话 → 获取 userId, modelId, clientId
  └── 构建 CommandDispatchRequest (agentId, userId, modelId, clientId)
  ↓
□ AI 调度执行
  ↓
◇ 需要创建客户端？
  ├── 是 → □ 工具回调 createClient
  │         └── 通过 sessionId 查会话 → 获取 userId
  └── 否 → 继续执行
  ↓
□ 返回 AI 回复
```

## 自检结论

- 已验证现有代码，`AgentChatSession` 实体已有 `userId`、`createUserId` 字段
- 已验证 `agent_chat_session` 表已有 `user_id`、`create_user_id` 列
- 需要新增：`model_id`、`client_id` 字段（表+实体+DTO）
- 需要修改：`CreateAgentChatSessionRequest`、`SendAgentChatMessageRequest`、`DefaultAgentChatService`、`AgentToolRegistry`、前端页面
- 无需变动：`CommandDispatchRequest`（已有 userId、modelId、clientId 字段）

## 重要文件索引表

| 文件                          | 路径                                                               | 改动类型 | 说明                                                  |
|-------------------------------|--------------------------------------------------------------------|----------|-------------------------------------------------------|
| CreateAgentChatSessionRequest | src/main/java/.../dto/agentChat/CreateAgentChatSessionRequest.java | 修改     | 增加 modelId、clientId 必填                           |
| SendAgentChatMessageRequest   | src/main/java/.../dto/agentChat/SendAgentChatMessageRequest.java   | 修改     | 移除 modelId、clientId                                |
| AgentChatSession              | src/main/java/.../entity/agentChatSession/AgentChatSession.java    | 修改     | 增加 modelId、clientId                                |
| AgentChatSessionResponse      | src/main/java/.../dto/agentChat/AgentChatSessionResponse.java      | 修改     | 增加 modelId、clientId                                |
| DefaultAgentChatService       | src/main/java/.../service/agentChat/DefaultAgentChatService.java   | 修改     | createSession 保存配置，dispatchAgent 从 session 获取 |
| AgentToolRegistry             | src/main/java/.../service/agent/AgentToolRegistry.java             | 修改     | createClient 从 session 获取 userId                   |
| public.sql                    | doc/sql/public.sql                                                 | 修改     | agent_chat_session 表增加 model_id、client_id         |
| AgentChatDto.ts               | web/src/dto/agentChat/AgentChatDto.ts                              | 修改     | DTO 字段调整                                          |
| AgentChatPage.tsx             | web/src/pages/AgentChatPage.tsx                                    | 修改     | 三个参数同一行，创建会话传完整参数                    |

## 编译验证记录

- 待执行

## 深度自检记录

### 一票否决项检查结果

| 检查项                 | 状态    | 说明                                         |
|------------------------|---------|----------------------------------------------|
| 表头强制不换行         | ✅ 通过 | 页面无 Table 组件，使用 List 展示            |
| 状态列 Tag 颜色        | ✅ 通过 | 页面无状态列展示                             |
| 操作列「更多」Dropdown | ✅ 通过 | 仅删除按钮，直接展示                         |
| 备注列截断 + Tooltip   | ✅ 通过 | 页面无备注列                                 |
| 防重复提交             | ✅ 通过 | 所有提交操作均使用 usePreventDoubleClickHook |

### Web 规则 Checklist 通过情况

| 检查项   | 状态    | 说明                                                                  |
|----------|---------|-----------------------------------------------------------------------|
| 搜索面板 | ✅ 通过 | className="simple-search-panel"，控件高度 36px，Space wrap 包裹       |
| 表单     | ✅ 通过 | 页面无表单弹窗                                                        |
| 工具栏   | ✅ 通过 | 页面无工具栏                                                          |
| 按钮     | ✅ 通过 | 主按钮/危险按钮使用正确，Popconfirm 用于删除确认                      |
| 反馈     | ✅ 通过 | 统一使用 ToastUtil                                                    |
| 认证     | ✅ 通过 | OAuth 认证由全局拦截器处理                                            |
| 代码风格 | ✅ 通过 | JSDoc 完整，useCallback/useMemo 优化，无过长链式调用                  |
| 接口对接 | ✅ 通过 | 创建会话传递 agentId/modelId/clientId，发送消息仅传 sessionId/content |

### 关键修复内容

- 移除 `SendAgentChatMessageRequestDto` 中的 `modelId`、`clientId` 可选字段
- `CreateAgentChatSessionRequestDto` 增加 `modelId`、`clientId` 必填字段
- `AgentChatPage.tsx` 将智能体、模型、客户端三个 Select 放在同一行
- 新建对话按钮 `disabled` 条件改为 `!selectedAgentId || !selectedModelId || !selectedClientId`
- 移除模型和客户端 Select 的 `allowClear` 属性（必填项不可清空）
- `handleCreateSession` 增加三参数校验
- `handleSend` 移除 `modelId`、`clientId` 传递逻辑