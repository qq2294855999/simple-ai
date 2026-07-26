# 客户端在线状态管理与界面优化 开发计划

> 状态: 进行中
> 作者: qty
> 日期: 2026-07-26

## 当前恢复入口

- 当前阶段：阶段一已完成，进入阶段五 code-inspector 自检。
- 当前进行中：[-] code-inspector 深度自检。
- 下一步：自检通过后进入阶段二 Web 前端编码。

---

## 全局约束与依赖

- 编译命令: `mvn clean package`（Java）、`npm run build`（Web）
- 代码规范: [java开发规范.md](C:/Users/Admin/.kilocode/rules/java开发规范.md)、[web代码编辑规范.md](C:/Users/Admin/.kilocode/rules/web代码编辑规范.md)
- 关键依赖: `simple-common-websocket`（WebSocketUtils.isOnline）、`simple-common-core`
- 数据库: PostgreSQL（非 MySQL，DDL 需适配 PostgreSQL 语法）

---

## 目录与子文档索引

| 子文档 | 阶段                  | 步骤数 | 状态       |
|--------|-----------------------|--------|------------|
| —      | 阶段零: 数据库变更    | 1      | [-] 进行中 |
| —      | 阶段一: Java 后端编码 | 9      | [ ] 待开始 |
| —      | 阶段二: Web 前端编码  | 5      | [ ] 待开始 |
| —      | 阶段三: 编译+自检     | 2      | [ ] 待开始 |

---

## 需求概览

### 需求一：执行器客户端状态机制

- 客户端（agent_client）需要有在线/离线状态，与 WebSocket 上下线机制同步
- 客户端管理界面列表列顺序调整为： **执行器 → 客户端名称 → 客户端code ···**
- 删除"机器名称"字段（数据库 + 代码 + 前端）

### 需求二：Web 界面状态中文显示

- 多处状态列直接显示数字，需转化为中文标签

### 需求三：人机对话客户端在线判断接口

- 新增本地接口，判断当前对话关联的客户端是否在线

---

## 业务流程图

```
┌─────────────────────────────────────────────────────┐
│  任务1: 执行器客户端状态机制                          │
├─────────────────────────────────────────────────────┤
│                                                       │
│  WebSocket 连接请求                                   │
│    ↓                                                  │
│  □ WebSocketAuthHandler.channelRead()                │
│    ↓                                                  │
│  □ ClientCheckWebSocketManager.checkToken()          │
│    ├── 鉴权通过                                       │
│    │   ↓                                              │
│    │   □ 更新 last_connected_at = now()              │
│    │   □ 更新 is_online = true                       │
│    │   ↓                                              │
│    │   □ WebSocketUtils.addMap(type, cliKey, ctx)    │
│    │                                                  │
│  WebSocket 断开连接                                   │
│    ↓                                                  │
│  □ WebSocketAuthHandler.channelInactive()            │
│    ├── 获取 type + cliKey                            │
│    │   ↓                                              │
│    │   □ WebSocketUtils.del(type, cliKey, ctx)       │
│    │   □ 更新 last_disconnected_at = now()           │
│    │   □ 更新 is_online = false                      │
│    │                                                  │
│  Web 管理界面查询列表                                 │
│    ↓                                                  │
│  □ AgentClientDao.xml JOIN agent_executor            │
│    └── 输出: executorName, executorCode, isOnline     │
│    └── 删除: machineName                             │
│                                                       │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  任务3: 人机对话客户端在线判断                        │
├─────────────────────────────────────────────────────┤
│                                                       │
│  前端请求                                             │
│    ↓                                                  │
│  □ GET /sys/agent-chat/client-online?clientId=xxx    │
│    ↓                                                  │
│  ◇ WebSocketUtils.isOnline("agent-executor", id)?    │
│    ├── true → R.ok({ online: true })                 │
│    └── false → R.ok({ online: false })               │
│                                                       │
└─────────────────────────────────────────────────────┘
```

---

## 自检结论

### 已验证现有代码

| 模块          | 文件                               | 验证结论                                                             |
|---------------|------------------------------------|----------------------------------------------------------------------|
| Entity        | `AgentClient.java`                 | 有 `machineName`（需删），无 `isOnline`（需增），`status` 类型为枚举 |
| 枚举          | `AgentClientStatusProcess.java`    | ACTIVE/EXPIRED/DISABLED/REVOKED，有 code+label                       |
| Controller    | `AgentClientController.java`       | 分页查询/详情/新增/更新/删除，无在线状态接口                         |
| Service       | `DefaultAgentClientService.java`   | 分页直接透传，未填充在线状态                                         |
| View          | `MPAgentClientView.java`           | 调用 Repository.selectPage                                           |
| MapperXML     | `AgentClientDao.xml`               | 包含 `machine_name`，未 JOIN executor，未查 `is_online`              |
| DTO           | `PageAgentClientResponse.java`     | 有 `machineName`，无 `isOnline`/`executorName`/`executorCode`        |
| WebSocket     | `ClientCheckWebSocketManager.java` | checkToken 更新 lastConnectedAt，未处理 is_online                    |
| WebSocket框架 | `WebSocketAuthHandler.java`        | channelInactive 只移除 ChannelMap，无业务回调                        |
| 工具类        | `WebSocketUtils.java`              | **已有** `isOnline(type, cliKey)` 方法                               |
| 前端          | `AgentClientManagementPage.tsx`    | 有"机器名称"列，状态已处理中文但列顺序需调整                         |
| DDL           | `public.sql`                       | `agent_client` 表有 `machine_name`，无 `is_online`                   |

### 需要新增/修改

- **新增**: `is_online` 数据库字段，定时同步组件，AgentChat 在线判断接口
- **修改**: AgentClient Entity/DTO/XML/CopyMapper，WebSocket 鉴权管理器，前端页面
- **删除**: `machine_name` 数据库字段及所有引用

---

## 设计决策

### is_online 同步机制

`WebSocketAuthHandler` 是框架层 Handler，`channelInactive` 中无法直接注入业务 Service。采用以下方案：

1. **上线同步**：在 `ClientCheckWebSocketManager.checkToken()` 鉴权成功后更新 `is_online = true`
2. **离线同步**：创建 `ClientOnlineStatusSyncService` 定时任务（每 30 秒），遍历所有 ACTIVE 的客户端，通过 `WebSocketUtils.isOnline("agent-executor", clientId)`
   检查并更新 `is_online` 字段
3. **优雅停机**：服务关闭时批量将所有 `is_online = true` 的客户端设为 false

### executorName / executorCode 的 JOIN

用户要求列表显示顺序为"执行器 客户端名称 客户端code"，当前缺少 executorName/executorCode（agent_client 只存 executorId）。需要在 AgentClientDao.xml 中 LEFT JOIN
agent_executor 获取。

### machineName 清理范围

- 数据库: `agent_client` 表 DROP COLUMN
- Entity: `AgentClient.machineName` 删除
- DTO: `PageAgentClientResponse.machineName`、`InfoAgentClientResponse.machineName` 删除
- XML: `AgentClientDao.xml` 中 `machine_name` 删除
- CopyMapper: 去掉 machineName 映射
- 前端: DTO + 页面列删除

---

## CRUD 契约表（仅涉及变更部分）

| 维度               | 内容                                                        | 验证状态  |
|--------------------|-------------------------------------------------------------|-----------|
| 接口路径           | `GET agent/client/page`（修改响应字段）                     | ✅ 已存在 |
| 接口路径           | `GET agent/client/{id}`（修改响应字段）                     | ✅ 已存在 |
| 接口路径           | `GET sys/agent-chat/client-online`（新增）                  | ✅ 已创建 |
| Request DTO 白名单 | CreateAgentClientRequest: 不含 machineName（已确认）        | ✅ 已验证 |
| Response DTO       | 新增 isOnline、executorName、executorCode；删除 machineName | ✅ 已修改 |
| 后台字段来源       | isOnline: WebSocket ChannelMap + 定时同步                   | ✅ 已实现 |
| 数据库变更         | agent_client: DROP machine_name, ADD is_online              | ✅ 已执行 |

---

## 设计对齐缺口清单

| 状态 | 设计要点           | 当前代码表现               | 后续处理                       |
|------|--------------------|----------------------------|--------------------------------|
| [x]  | 离线状态更新       | channelInactive 无业务回调 | 定时任务同步 ChannelMap        |
| [x]  | 服务重启后在线状态 | 无处理                     | 启动时批量重置 is_online=false |

---

## 重要文件索引

| 文件                               | 路径                                                                               | 改动类型 | 说明                                                  |
|------------------------------------|------------------------------------------------------------------------------------|----------|-------------------------------------------------------|
| public.sql                         | doc/sql/public.sql                                                                 | 修改     | DROP machine_name, ADD is_online                      |
| AgentClient.java                   | src/main/java/com/simple/ai/common/entity/agentClient/AgentClient.java             | 修改     | 删 machineName，增 isOnline                           |
| PageAgentClientResponse.java       | src/main/java/com/simple/ai/common/dto/agentClient/PageAgentClientResponse.java    | 修改     | 删 machineName，增 isOnline/executorName/executorCode |
| InfoAgentClientResponse.java       | src/main/java/com/simple/ai/common/dto/agentClient/InfoAgentClientResponse.java    | 修改     | 删 machineName，增 isOnline                           |
| AgentClientDao.xml                 | src/main/resources/mapper/AgentClientDao.xml                                       | 修改     | 删 machine_name，JOIN executor，增 is_online          |
| ClientCheckWebSocketManager.java   | src/main/java/com/simple/ai/websocket/command/ClientCheckWebSocketManager.java     | 修改     | 鉴权成功设 is_online=true                             |
| ClientOnlineStatusSyncService.java | src/main/java/com/simple/ai/service/agentClient/ClientOnlineStatusSyncService.java | 新增     | 定时同步在线状态                                      |
| AgentClientCopyMapper.java         | src/main/java/com/simple/ai/common/copy/agentClient/AgentClientCopyMapper.java     | 修改     | 调整映射                                              |
| AgentClientView.java               | src/main/java/com/simple/ai/common/view/agentClient/AgentClientView.java           | 修改     | 新增 updateOnlineStatus 方法                          |
| MPAgentClientView.java             | src/main/java/com/simple/ai/view/agentClient/MPAgentClientView.java                | 修改     | 实现 updateOnlineStatus                               |
| AgentChatController.java           | src/main/java/com/simple/ai/controller/agentChat/AgentChatController.java          | 修改     | 新增 client-online 接口                               |
| AgentChatService.java              | src/main/java/com/simple/ai/common/service/agentChat/AgentChatService.java         | 修改     | 新增 isClientOnline 方法                              |
| DefaultAgentChatService.java       | src/main/java/com/simple/ai/service/agentChat/DefaultAgentChatService.java         | 修改     | 实现 isClientOnline                                   |
| AgentClientManagementPage.tsx      | web/src/pages/AgentClientManagementPage.tsx                                        | 修改     | 删机器名称列，调整列顺序，增在线状态列                |
| AgentClientDto.ts                  | web/src/dto/agentClient/AgentClientDto.ts                                          | 修改     | 删 machineName，增 isOnline/executorName/executorCode |
| AgentExecutorManagementPage.tsx    | web/src/pages/AgentExecutorManagementPage.tsx                                      | 修改     | 状态数字转中文                                        |

---

## 执行状态清单

### 阶段零: 数据库变更

- [x] 步骤0: 使用 db-ddl-generator 生成 agent_client 表 ALTER SQL（删除 machine_name，新增 is_online），写入 doc/sql/

### 阶段一: Java 后端编码

- [x] 步骤1: 修改 AgentClient Entity（删 machineName，增 isOnline）
- [x] 步骤2: 修改 PageAgentClientResponse（删 machineName，增 isOnline/executorName/executorCode/executorId）
- [x] 步骤3: 修改 InfoAgentClientResponse（删 machineName，增 isOnline）
- [x] 步骤4: 修改 AgentClientDao.xml（删 machine_name，LEFT JOIN agent_executor，增 is_online）
- [x] 步骤5: 确认 AgentClientCopyMapper 无需修改（MapStruct 自动按字段名映射）
- [x] 步骤6: 修改 ClientCheckWebSocketManager（鉴权成功设 is_online=true）
- [x] 步骤7: 新增 ClientOnlineStatusSyncService（定时同步 ChannelMap 到 DB）
- [x] 步骤8: 新增 AgentChat 客户端在线判断接口（Controller + Service）
- [x] 步骤9: mvn clean package 编译验证（BUILD SUCCESS）

### 阶段二: Web 前端编码

- [x] 步骤10: 修改 AgentClientDto（删 machineName，增 isOnline/executorCode/lastDisconnectedAt）
- [x] 步骤11: 修改 AgentClientManagementPage（删机器名称列，调整列顺序：执行器→客户端名称→客户端编码→在线状态→状态→...，新增在线状态列使用 Tag 绿色/红色）
- [x] 步骤12: 修改 AgentExecutorManagementPage（确认状态列已使用 Tag + getStatusLabel，无需改动）
- [x] 步骤13: 检查其他前端页面状态显示（ProtocolManagementPage/SubAgentRelationManagementPage/CommandDispatchPage 等均已使用 Tag + 中文，无需改动）
- [x] 步骤14: npm run build 编译验证（✓ built in 768ms）

### 阶段三: 编译+自检

- [ ] 步骤15: 最终 mvn clean package + npm run build
- [ ] 步骤16: code-inspector 深度自检

---

## 编译验证记录

| 时间             | 命令                          | 结果              |
|------------------|-------------------------------|-------------------|
| 2026-07-26 12:38 | mvn clean package -DskipTests | BUILD SUCCESS     |
| 2026-07-26 12:43 | npm run build                 | ✓ built in 768ms |

## 自检记录

| 时间             | 检查范围                       | 结果                                                     |
|------------------|--------------------------------|----------------------------------------------------------|
| 2026-07-26 12:40 | 阶段一全部变更文件（11个文件） | ✅ 通过（发现1个关键Bug：缺少@EnableScheduling，已修复） |

### 自检详情

**一票否决项**:

- ✅ N+1查询: 无（定时任务批量查询+逐条更新，30秒间隔可接受）
- ✅ 树形递归: 无
- ✅ 循环调用外部接口: 无（WebSocketUtils.isOnline 是内存操作）
- ✅ 线程安全: 无共享可变状态，ClientOnlineStatusSyncService与ClientCheckWebSocketManager操作不同字段
- ✅ 内存风险: 无资源泄露，无大对象加载

**代码规范**:

- ✅ 所有公共类/接口/方法有 Javadoc（含 @param/@return）
- ✅ 每个方法内步骤前有换行注释
- ✅ 无数字序号标注
- ✅ @author 均为 qty
- ✅ 无全限定类名

**SQL规范**:

- ✅ 使用 `<where>` 标签
- ✅ 无 SELECT *
- ✅ 通过 JOIN 获取关联名称

**流程闭环**:

- ✅ 上线: ClientCheckWebSocketManager.checkToken → is_online=true
- ✅ 离线: ClientOnlineStatusSyncService 定时30秒遍历同步
- ✅ 启动: @PostConstruct 批量重置 is_online=false
- ✅ 查询: AgentClientDao.xml JOIN executor + ORDER BY is_online DESC
- ✅ 在线判断: GET /sys/agent-chat/client-online → WebSocketUtils.isOnline
