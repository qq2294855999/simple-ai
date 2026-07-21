# 智能体—技能—原子命令—执行器—记忆闭环 联合开发计划

## 项目概述

**范围**：`simple-ai`（Java/Spring Boot） + `simple-rpa-win`（C#/.NET 8）+ `simple-common-websocket` 三项目协同改造。

**核心目标**：实现"用户对话 → AI规划/记忆命中 → 精确路由执行器 → 命令执行回执 → 记忆沉淀/修订"的完整闭环。

**原则**：开发阶段，不兼容旧模型，不合适/旧的代码直接删除。

---

## 一、最终确认的设计决策

| # | 决策项        | 结论                                                                                      |
|---|---------------|-------------------------------------------------------------------------------------------|
| A | 实体模型      | agent_executor（执行器类型）+ agent_client（客户端实例，绑定用户+密钥）                   |
| B | WebSocket鉴权 | type=agent-executor, cliKey=clientId（非userId）, token=secret; 重写CheckWebSocketManager |
| C | 路由策略      | 按cliKey=clientId **点对点发送**，禁止广播; 同clientId新连接踢旧连接                      |
| D | 命令路由      | atomic_command.skill_id 保留但改为**可选分类标签**; 主路由依据是 executor_id              |
| E | 技能返回参数  | return_data_format → plan_output_schema + observation_schema                              |
| F | 旧代码        | cliKey="default"、广播执行、非版本化记忆 → 全部删除                                       |
| G | 记忆版本      | agent_memory_version + agent_memory_version_detail 替代旧 agent_memory_detail             |
| H | 通用协议      | SEP v1.0（Simple Executor Protocol），system.capability 作为固定原子命令同步命令列表      |
| I | C#执行器      | 需要适配新协议：COMMAND_BATCH/COMMAND_RESULT/system.capability/HEARTBEAT                  |
| J | SQL           | 已就绪（public.sql），含全部中文注释                                                      |

---

## 二、数据库变更（已完成）

表已在 [`doc/sql/public.sql`](doc/sql/public.sql) 中完整定义，含中文注释：

| 类型 | 表名                          | 说明                                                                |
|------|-------------------------------|---------------------------------------------------------------------|
| 新增 | `agent_executor`              | 执行器类型（如 WINDOWS_RPA）                                        |
| 新增 | `agent_client`                | 客户端实例（密钥+状态+过期时间，WebSocket cliKey来源）              |
| 新增 | `agent_memory_version`        | 记忆版本（DRAFT/PUBLISHED/RETIRED）                                 |
| 新增 | `agent_memory_version_detail` | 版本步骤（按sequence_no排序，可直接编译COMMAND_BATCH）              |
| 改造 | `agent_definition`            | +user_id                                                            |
| 改造 | `agent_skill`                 | +user_id, plan_output_schema, observation_schema                    |
| 改造 | `atomic_command`              | +user_id, executor_id                                               |
| 改造 | `agent_memory`                | +user_id                                                            |
| 改造 | `agent_chat_session`          | +user_id                                                            |
| 改造 | `task`                        | +user_id, client_id, memory_version_id, dispatch_id                 |
| 改造 | `task_detail`                 | +command_id, atomic_command_id, client_id, sequence_no, dispatch_id |

---

## 三、通用执行器协议 SEP v1.0

### 3.1 消息外层

```jsonc
{ "messageType": "COMMAND_BATCH | COMMAND_RESULT | HEARTBEAT | HEARTBEAT_ACK", "payload": {} }
```

### 3.2 COMMAND_BATCH（Server → Executor）

```jsonc
{
  "messageType": "COMMAND_BATCH",
  "payload": {
    "dispatchId": "snowflake_id", "taskId": "task_id", "clientId": "client_id",
    "stopOnFailure": true, "minDelayMs": 100, "maxDelayMs": 500,
    "commands": [{
      "commandId": "snowflake_id", "sequenceNo": 10,
      "atomicCommandCode": "window.find", "args": {},
      "timeoutMs": 10000, "idempotencyKey": "taskId+seq"
    }]
  }
}
```

### 3.3 COMMAND_RESULT（Executor → Server）

```jsonc
{
  "messageType": "COMMAND_RESULT",
  "payload": {
    "dispatchId": "...", "taskId": "...", "commandId": "...", "sequenceNo": 10,
    "success": true, "message": "执行说明",
    "data": {}, "error": {"code":"WINDOW_NOT_FOUND","detail":"...","recoverable":true},
    "startedAt": "ISO8601", "finishedAt": "ISO8601"
  }
}
```

### 3.4 内置系统命令

- **system.capability**：返回执行器支持的全部原子命令列表（code + name + description + argsSchema + resultSchema + riskLevel + isIdempotent）
- **system.health**：返回执行器健康状态

### 3.5 握手后立即同步

```
WebSocket连接 → 鉴权通过 → 服务端下发 system.capability → 执行器返回命令列表 → 服务端 upsert atomic_command 表
```

---

## 四、完整调用链

```
用户 → 聊天页面 → AgentChatController.sendStream() (SSE)
  → DefaultAgentChatService.sendStream()
    → 保存用户消息 → CommandDispatchService.dispatchStream()
      → 创建Task → AgentContextAssembler.assemble()
        → 加载: AgentDefinition + Rules + Skills + Memories + SubAgentRelations
      → AgentMemoryMatcher.match()
        ├── HIT → executeMemorySteps()
        │         → 加载 memory_version → memory_version_detail[] (按sequence_no排序)
        │         → 预加载atomic_command（按executor_id过滤）
        │         → 编译ExecutorCommandBatchRequest
        │         → 先注册等待器(commandId) → 再WebSocket点对点发送
        │         → 等待COMMAND_RESULT → 持久化task_detail → 返回结果
        │
        └── MISS → executeAiExploration()
                   → AI生成AgentExecutionPlan
                   → 校验: 命令归属技能 + 客户端支持executor_type + argsSchema合规
                   → 逐步执行(循环): 发送→回执→AI观察→继续/调整/完成
                   → 成功后触发记忆沉淀(如果规则要求)
                     → AI提炼最短链 → memory_version(DRAFT) → 确认→PUBLISH
```

---

## 五、C# 执行器改造清单

### 5.1 当前协议 vs 新协议对比

| 维度              | 当前C#                                              | 新协议SEP v1.0                                                                                      | 改动                |
|-------------------|-----------------------------------------------------|-----------------------------------------------------------------------------------------------------|---------------------|
| 消息外层          | 无 messageType                                      | `{messageType, payload}`                                                                            | **新增外层包装**    |
| 单条命令          | `{Command, TraceId, Args}`                          | `{commandId, sequenceNo, atomicCommandCode, args, timeoutMs}`                                       | **字段重命名+扩展** |
| 回执              | `{Success, Message, TraceId, Data, Error}`          | `{dispatchId, taskId, commandId, sequenceNo, success, message, data, error, startedAt, finishedAt}` | **扩展审计字段**    |
| 批量命令          | `{Commands, MinDelayMs, MaxDelayMs, StopOnFailure}` | 移至 `payload.commands[]` + `payload.dispatchId/taskId`                                             | **外层包装变化**    |
| system.capability | 无（需新增）                                        | 标准内置命令                                                                                        | **新增**            |
| HEARTBEAT         | 无（需新增）                                        | 标准心跳                                                                                            | **新增**            |

### 5.2 C# 端需要修改的文件

| 文件                        | 改动                                                                         |
|-----------------------------|------------------------------------------------------------------------------|
| `WebSocketClientService.cs` | 解析外层 messageType; 新增 HEARTBEAT 响应; system.capability 处理            |
| `ExecuteCommandRequest.cs`  | `Command` → 保持不变（内部用）, 但解析入口改为 COMMAND_BATCH                 |
| `ExecuteCommandResponse.cs` | 新增 `DispatchId`/`TaskId`/`CommandId`/`SequenceNo`/`StartedAt`/`FinishedAt` |
| `BatchCommandItem.cs`       | 新增 `CommandId`/`SequenceNo`/`TimeoutMs`/`IdempotencyKey`                   |
| `CommandDispatcher.cs`      | 支持 system.capability 和 system.health 内置命令                             |
| `Program.cs`                | 托盘右键菜单：服务端地址 + 客户端ID + 客户端密钥 输入框; 连接状态展示        |
| `AgentOptions.cs`           | 新增 `ClientId`/`ClientSecret`/`ServerUrl` 配置项                            |

### 5.3 C# 端消息处理流程（改造后）

```
Received WebSocket message
  → 解析 JSON → messageType 分发:
    ├── "COMMAND_BATCH" → CommandDispatcher.ExecuteBatchAsync()
    │     → 遍历 commands[]
    │     → Thread.Sleep(random(minDelay, maxDelay))
    │     → handler.ExecuteAsync(request) → ExecuteCommandResponse
    │     → 包装为 COMMAND_RESULT → WebSocket Send
    │     → if !success && stopOnFailure → break
    ├── "HEARTBEAT" → 立即回复 HEARTBEAT_ACK
    └── 未知 → 忽略
```

---

## 六、Java 端（simple-ai）实施步骤清单

> **执行策略**：此大章节通过 `new_task` 创建子任务逐步骤执行，避免单次会话上下文溢出。

### 步骤1：simple-common-websocket 鉴权扩展

- [x] **新建** `ClientCheckWebSocketManager.java`（继承 DefaultCheckWebSocketManager）
    - 路径：`src/main/java/com/simple/ai/websocket/`
    - 重写 `checkToken(token, type, cliKey)`:
        - 仅处理 `type="agent-executor"`
        - 查询 `agent_client` WHERE id=cliKey FOR UPDATE
        - BCrypt.verify (token, client_secret_hash)
        - 校验 status=ACTIVE, expire_time > now ()
        - 过期 → UPDATE status=EXPIRED → 返回false拒绝
        - 通过 → UPDATE last_connected_at → 返回true

### 步骤2：新增实体类（4个）

- [x] `AgentExecutor.java` → `common/entity/agentExecutor/`
- [x] `AgentClient.java` → `common/entity/agentClient/`
- [x] `AgentMemoryVersion.java` → `common/entity/agentMemoryVersion/`
- [x] `AgentMemoryVersionDetail.java` → `common/entity/agentMemoryVersionDetail/`

### 步骤3：新增枚举类

- [x] `AgentClientStatusProcess.java` → `common/enums/`（ACTIVE/EXPIRED/DISABLED/REVOKED）
- [x] `AgentMemoryVersionStatusProcess.java` → `common/enums/`（DRAFT/PUBLISHED/RETIRED）

### 步骤4：新增 DTO（Request/Response）✅

> 每个表至少需要：Page/Create/Update/Delete/FindOne/FindAll/Info 系列DTO

- [x] `common/dto/agentExecutor/`（8个DTO，已完成）
- [x] `common/dto/agentClient/`（8个DTO，已完成）
- [x] `common/dto/agentMemoryVersion/`（8个DTO，已完成）
- [x] `common/dto/agentMemoryVersionDetail/`（8个DTO，已完成）

### 步骤5：新增 CopyMapper ✅

- [x] `AgentExecutorCopyMapper.java`
- [x] `AgentClientCopyMapper.java`
- [x] `AgentMemoryVersionCopyMapper.java`
- [x] `AgentMemoryVersionDetailCopyMapper.java`

### 步骤6：新增 View 接口 + MP 实现 ✅

- [x] `AgentExecutorView.java` + `MPAgentExecutorView.java`
- [x] `AgentClientView.java` + `MPAgentClientView.java`
- [x] `AgentMemoryVersionView.java` + `MPAgentMemoryVersionView.java`
- [x] `AgentMemoryVersionDetailView.java` + `MPAgentMemoryVersionDetailView.java`

### 步骤7：新增 Repository 接口 + Mapper XML ✅

- [x] `AgentExecutorRepository.java` + `AgentExecutorDao.xml`
- [x] `AgentClientRepository.java` + `AgentClientDao.xml`
- [x] `AgentMemoryVersionRepository.java` + `AgentMemoryVersionDao.xml`
- [x] `AgentMemoryVersionDetailRepository.java` + `AgentMemoryVersionDetailDao.xml`

### 步骤8：新增 Service 接口 + Default 实现 ✅

- [x] `AgentExecutorService.java` + `DefaultAgentExecutorService.java`
- [x] `AgentClientService.java` + `DefaultAgentClientService.java`
    - **核心**：创建客户端时生成secret → BCrypt哈希 → 仅返回一次明文
    - 计算过期时间：数字 + 单位 (DAY/WEEK/MONTH/YEAR) → expire_time
- [x] `AgentMemoryVersionService.java` + `DefaultAgentMemoryVersionService.java`
    - **核心**：publish/retire 版本状态变更（DRAFT→PUBLISHED→RETIRED）

### 步骤9：新增 Controller ✅

- [x] `AgentExecutorController.java`（执行器类型管理，含协议展示）
- [x] `AgentClientController.java`（客户端管理，密钥仅创建时返回）
- [x] `AgentMemoryVersionController.java`（记忆版本管理，含 publish/retire 接口）

### 步骤10：改造 WebSocket 端点 ✅

- [x] **重构** `AgentExecutorEndpoint.java`
    - 删除 `cliKey="default"` 硬编码
    - 改为动态接收所有 agent-executor 类型消息
    - 解析 COMMAND_RESULT（非旧的 AgentExecutorResponse）
    - 按 commandId 完成等待器
- [x] **重构** `AtomicCommandResponseWaiter.java`
    - 按 commandId 注册/完成（非 taskId）
  - 超时自动清理（Delayed 机制）
  - 断连自动清理（clearByClientId）

### 步骤11：改造命令执行器 ✅

- [x] **重构** `DefaultAtomicCommandExecutor.java`
    - 删除广播逻辑（改为 sendMsg (type, cliKey, msg)）
    - **先注册等待器 → 再发送**（修复竞态BUG）
    - 支持批量命令编译（ExecutorCommandBatchRequest）
    - 支持 system.capability 命令处理

### 步骤12：改造命令调度服务 ✅

- [x] **重构** `DefaultCommandDispatchService.java`
    - 删除 `isAiGoalAchieved()` 和 `containsAnyText()` 方法
    - AI 探索路径：改为输出结构化 AgentExecutionPlan JSON
    - 记忆执行路径：准备加载 memory_version 框架
    - 新增 client 选择逻辑（resolveClientIdIfAbsent）
    - 新增记忆沉淀触发逻辑（triggerMemoryPrecipitation）

### 步骤13：改造上下文装配器 ✅

- [x] **修改** `AgentContextAssembler.java`
    - 按 user_id 过滤所有资产（DTO 参数待扩展）
    - 加载 executor 能力信息

### 步骤14：改造聊天服务 ✅

- [x] **修改** `DefaultAgentChatService.java`
    - 透传 clientId
    - SendAgentChatMessageRequest 新增 clientId/memoryAction 字段

### 步骤15：新增 WebSocket 命令DTO ✅

- [x] `ExecutorCommandBatchRequest.java` → `common/dto/command/`
- [x] `ExecutorCommandResultResponse.java` → `common/dto/command/`
- [x] `ExecutorCommandItem.java` → `common/dto/command/`

---

## 七、Web 前端实施步骤清单 ✅

> **执行策略**：此大章节通过 `new_task` 创建子任务逐步骤执行，避免单次会话上下文溢出。

### 步骤16：新增页面 ✅

- [x] **新建** `AgentExecutorManagementPage.tsx`（执行器类型管理 + 协议展示）
- [x] **新建** `AgentClientManagementPage.tsx`（客户端管理：创建/密钥展示/状态/过期）
- [x] **新建** `AgentMemoryVersionPage.tsx`（记忆版本管理）

### 步骤17：改造现有页面 ✅

- [x] **修改** `AgentChatPage.tsx`（新增客户端选择器 + clientId 透传）
- [x] **修改** `AtomicCommandManagementPage.tsx`（新增 executor_id 筛选/展示/选择）

### 步骤18：新增/修改 API 文件 ✅

- [x] `agentExecutorApi.ts`
- [x] `agentClientApi.ts`
- [x] `agentMemoryVersionApi.ts`
- [x] 修改 `agentChatApi.ts`（clientId 透传）

### 步骤19：新增/修改 DTO 文件 ✅

- [x] `AgentExecutorDto.ts`
- [x] `AgentClientDto.ts`
- [x] `AgentMemoryVersionDto.ts`

### 步骤20：导航菜单更新 ✅

- [x] **修改** 左侧菜单配置 → 新增"执行器与客户端"分组（含执行器类型管理、客户端管理）

---

### 阶段三D：C# 执行器 SEP v1.0 实施计划（2026-07-21）

**目标**：使 `simple-rpa-win` 通过 Java 服务端已确定的 SEP v1.0 协议，以客户端 ID 点对点连接、接收批量命令，并逐项回传可关联的执行结果。

```text
Java 服务端 COMMAND_BATCH
  ↓
□ C# WebSocket 客户端解析 { messageType, payload }
  ↓
◇ messageType
  ├── COMMAND_BATCH → □ 逐条执行（UIA 主路径 / Win32 兜底）→ □ COMMAND_RESULT
  ├── HEARTBEAT → □ HEARTBEAT_ACK
  └── 其他 → □ 记录协议警告并忽略
  ↓
Java 按 commandId 完成等待器
  ↓
AI 根据结构化结果继续决策
```

**实现顺序与影响范围**：

1. 在 `Common/Dtos` 增加 SEP 外层消息、批量请求项、执行结果、能力和健康状态模型；字段以 Java 的 `ExecutorCommandBatchRequest`、`ExecutorCommandItem`、
   `ExecutorCommandResultResponse` 为准。
2. 将 `WebSocketClientService` 的旧 `type/data` 协议整体替换为 `messageType/payload` 协议；移除反射发送 Ping 的不稳定实现，改为应用层心跳与安全串行发送。
3. 在 `CommandDispatcher` 内置 `system.capability` 与 `system.health`，能力响应由已注册处理器的命令元数据稳定生成；单命令执行保持现有处理器架构。
4. 扩充 `AgentOptions` 为 `ServerUrl`、`ClientId`、`ClientSecret`，连接 URI 使用现有服务端约定的 `type=agent-executor`、`cliKey`、`token` 查询参数；日志只记录脱敏后的服务端地址与客户端
   ID。
5. 基于现有 Windows Forms 托盘程序新增安全的连接配置对话框与连接状态文本，不将密钥回显或写入日志；运行时重连读取更新后的线程安全配置快照。
6. 每次 C# 代码修改后执行 `dotnet build`；查找并执行测试；最终执行指定的 win-x64 自包含单文件发布；完成后回写本计划的实际结果。

**风险与控制**：服务端 WebSocket 框架对入站消息可能仍有额外包装要求，已按 Java `AgentExecutorEndpoint` 的 `WebSocketRequest<T>` 及当前服务端发送实现验证。命令超时使用每条命令专属
`CancellationTokenSource`，避免停止整个客户端接收循环；发送通过 `SemaphoreSlim` 串行化，避免并发 `ClientWebSocket.SendAsync` 竞态。

### 阶段三D 实际实施与验证结果（2026-07-21）

- [x] 新增 Java `SepMessage<T>`，Java 服务端下发 `COMMAND_BATCH` 时统一发送 SEP 业务外层 `{messageType, payload}`；`AgentExecutorEndpoint` 仅接收 `COMMAND_RESULT`
  并按 `commandId` 完成等待器。
- [x] C# 新增 SEP v1.0 批量命令、单项命令、执行回执、能力和健康状态模型，完整保留 `dispatchId`、`taskId`、`commandId`、`sequenceNo`、`startedAt`、`finishedAt` 等关联字段。
- [x] C# `WebSocketClientService` 已实现 `COMMAND_BATCH` 顺序执行、每条命令独立 `COMMAND_RESULT` 回传、`HEARTBEAT` → `HEARTBEAT_ACK`，并以每命令专属取消源落实
  `timeoutMs`。
- [x] 已确认 `simple-common-websocket` 的入站传输约束：Netty 将客户端文本 JSON 反序列化为 `WebSocketRequest`，业务数据必须位于传输外层 `data` 字段。因此 C# 上行发送
  `{ "data": { "messageType": "COMMAND_RESULT", "payload": { ... } } }`；其中 `data` 为框架传输包装，内部仍严格使用 SEP v1.0 业务外层。Java 服务端下行由
  `WebSocketUtils.sendMsg` 直接发送 SEP JSON，C# 直接按 `messageType/payload` 解析。
- [x] C# `CommandDispatcher` 已内置 `system.capability` 与 `system.health`；能力清单由已注册命令处理器稳定生成，健康状态包含进程和检查时间。
- [x] `AgentOptions`、`appsettings.json` 和现有托盘程序已支持 `ServerUrl`、`ClientId`、`ClientSecret`、连接状态与配置入口；连接凭据使用 `type=agent-executor`、
  `cliKey`、`token` 查询参数，日志与托盘均不回显客户端密钥。
- [x] C# `dotnet build`：成功，0 warning、0 error（最终验证：2026-07-21）。
- [x] C# `dotnet test`：已执行；解决方案未发现可运行测试项目。
- [x] C# `dotnet publish -c Release -r win-x64 --self-contained true /p:PublishSingleFile=true /p:IncludeNativeLibrariesForSelfExtract=true`：成功。发布目录：
  `C:/start/net/simple-rpa-win/src/Simple.Automation.Agent/bin/Release/net8.0-windows10.0.19041.0/win-x64/publish/`。
- [x] Java `mvn clean compile`：成功（2026-07-21，333 个源文件，BUILD SUCCESS）。
- [x] 深度自检：已检查协议双向数据链路、传输外层与 SEP 业务外层边界、关联字段、单命令取消链路、并发发送互斥、连接资源释放、敏感凭据日志保护及托盘状态访问；未发现阻断性交付问题。

---

## 八、旧代码删除清单

| 删除项                               | 文件                                 | 替代方案              |
|--------------------------------------|--------------------------------------|-----------------------|
| `cliKey="default"` 硬编码            | `AgentExecutorEndpoint.java`         | 动态接收所有cliKey    |
| 按type广播执行                       | `DefaultAtomicCommandExecutor.java`  | 点对点发送            |
| 先发送后注册等待器                   | `DefaultAtomicCommandExecutor.java`  | 先注册后发送          |
| `isAiGoalAchieved()`                 | `DefaultCommandDispatchService.java` | 结构化计划校验        |
| `agent_memory_detail` 作为可执行步骤 | 实体/View/Service                    | memory_version_detail |
| 技能 `return_data_format`            | `agent_skill` 表 → 改名为新字段      |

---

## 九、编译与构建

每个阶段完成后执行：

```bash
# Java
cd C:/start/simple-ai && mvn clean compile

# Web
cd C:/start/simple-ai/web && npm run build

# C# 执行器
cd C:/start/net/simple-rpa-win && dotnet build
```

---

## 十、当前进度状态

- [x] 阶段一：需求分析与整体设计（已确认）
- [x] 阶段二：计划技术验证（通过）
- [x] SQL 数据库变更（已完成）
- [x] 本开发计划文档（已完成）
- [x] 阶段三A：simple-common-websocket 鉴权重写（步骤1）
- [x] 阶段三B-步骤2-15：Java 后端全部完成（2026-07-21编译通过）
- [x] 阶段三C：Web 前端（步骤16-20，npm run build 通过）
- [x] 阶段三D：C# 执行器改造（第五章，SEP v1.0 对接、Java 协议同步、build/test/publish 通过）
- [x] 阶段四：mvn clean package + npm run build（2026-07-21 全部通过）
- [x] 阶段五：Java/Web 深度自检（2026-07-21 全部通过）
    - Java 自检修复：
        - 🔴 N+1 查询（loadEnabledAtomicCommands 循环按技能查询 → 改为 skillIds 批量 IN 查询）
        - 🟡 死代码（findMatchedAtomicCommand (String) 单参方法未被调用 → 移除）
    - Web 自检修复：
        - ❌ AtomicCommandManagementPage 状态列未使用 Tag 颜色 → 添加 `<Tag color>`
        - ❌ AtomicCommandManagementPage 操作列 2 个按钮未使用 Dropdown → 改为"编辑"+"更多"模式

---

## 十一、文件索引速查

| 项目                    | 关键文件                                                           | 改动类型                        |
|-------------------------|--------------------------------------------------------------------|---------------------------------|
| simple-common-websocket | `CheckWebSocketManager.java`                                       | 参考接口                        |
| simple-common-websocket | `WebSocketUtils.java`                                              | 使用 sendMsg(type, cliKey, msg) |
| simple-common-websocket | `DefaultCheckWebSocketManager.java`                                | 继承基类                        |
| simple-ai               | `AgentExecutorEndpoint.java`                                       | **重构**                        |
| simple-ai               | `DefaultAtomicCommandExecutor.java`                                | **重构**                        |
| simple-ai               | `DefaultCommandDispatchService.java`                               | **重构**                        |
| simple-ai               | `AgentContextAssembler.java`                                       | 修改                            |
| simple-ai               | `DefaultAgentChatService.java`                                     | 修改                            |
| simple-ai               | `AtomicCommandResponseWaiter.java`                                 | **重构**                        |
| simple-rpa-win          | `WebSocketClientService.cs`                                        | 修改                            |
| simple-rpa-win          | `ExecuteCommandResponse.cs`                                        | 扩展字段                        |
| simple-rpa-win          | `CommandDispatcher.cs`                                             | 新增 system.capability          |
| simple-rpa-win          | `Program.cs`                                                       | 托盘菜单                        |
| Web                     | `AgentChatPage.tsx`                                                | 客户端选择器                    |
| Web                     | `AgentClientManagementPage.tsx`                                    | **新建**                        |
| Web                     | `AgentExecutorManagementPage.tsx`                                  | **新建**                        |
| **步骤8-9新增**         |                                                                    |                                 |
| simple-ai               | `common/service/agentExecutor/AgentExecutorService.java`           | **新建**                        |
| simple-ai               | `service/agentExecutor/DefaultAgentExecutorService.java`           | **新建**                        |
| simple-ai               | `common/service/agentClient/AgentClientService.java`               | **新建**                        |
| simple-ai               | `service/agentClient/DefaultAgentClientService.java`               | **新建**                        |
| simple-ai               | `common/service/agentMemoryVersion/AgentMemoryVersionService.java` | **新建**                        |
| simple-ai               | `service/agentMemoryVersion/DefaultAgentMemoryVersionService.java` | **新建**                        |
| simple-ai               | `controller/agentExecutor/AgentExecutorController.java`            | **新建**                        |
| simple-ai               | `controller/agentClient/AgentClientController.java`                | **新建**                        |
| simple-ai               | `controller/agentMemoryVersion/AgentMemoryVersionController.java`  | **新建**                        |
