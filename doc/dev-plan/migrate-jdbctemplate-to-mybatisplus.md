# JdbcTemplate 迁移至 MyBatis Plus + DTO 中文注释改造

## 当前总体状态

- 当前阶段：阶段三，深度自检（已完成）。
- 当前状态：全部步骤已完成，`mvn clean compile` 编译通过（0 error）。
- 下一步：无（任务已完成）。

---

## 重点业务流程说明

### 1. JdbcTemplate → MyBatis Plus 迁移

`ClientCheckWebSocketManager.java` 负责 WebSocket 连接鉴权，当前直接使用 `JdbcTemplate` 查询 `agent_client` 表。项目已有完整的 `AgentClientView` +
`AgentClientRepository`(BaseMapper) + `MPAgentClientView` MyBatis Plus 基础设施，改造路径：

```
改造前:
  ClientCheckWebSocketManager
    → @Autowired JdbcTemplate
    → 手写 SQL (SELECT/UPDATE)

改造后:
  ClientCheckWebSocketManager
    → @Autowired AgentClientView
    → 调用 AgentClientView.findByIdWithLock() / updateById()
      → MPAgentClientView
        → AgentClientRepository (BaseMapper + 自定义SQL)
```

### 2. DTO 英文注释 → 中文注释

7 个 DTO 文件的 `@Schema(description = "...")`、类 Javadoc、`@NotEmpty(message = "...")` 使用了英文描述，需统一改为中文。

---

## 流程图

```
□ 需求分析完成
  ↓
◇ JdbcTemplate 是否还有使用？
  ├── 是 → □ AgentClientView 加方法 (findByIdWithLock / customUpdate)
  │         □ AgentClientRepository 加 SQL 方法
  │         □ MPAgentClientView 实现
  │         □ ClientCheckWebSocketManager 改造
  └── 否 → 跳过
  ↓
◇ DTO 注释是否是中文？
  ├── 否 → □ 逐个 DTO 文件替换英文为中文注释
  └── 是 → 跳过
  ↓
□ mvn clean compile 编译验证
  ↓
□ 深度自检
```

---

## 文件清单

| 文件                                   | 路径                                                                                                   | 改动类型 | 说明                                                |
|----------------------------------------|--------------------------------------------------------------------------------------------------------|----------|-----------------------------------------------------|
| AgentClientView                        | `src/main/java/com/simple/ai/common/view/agentClient/AgentClientView.java`                             | 修改     | 增加 findByIdWithLock、updateById(AgentClient) 方法 |
| AgentClientRepository                  | `src/main/java/com/simple/ai/view/agentClient/AgentClientRepository.java`                              | 修改     | 增加 selectByIdWithLock SQL 方法                    |
| AgentClientDao.xml                     | `src/main/resources/mapper/AgentClientDao.xml`                                                         | 修改     | 增加 selectByIdWithLock SQL                         |
| MPAgentClientView                      | `src/main/java/com/simple/ai/view/agentClient/MPAgentClientView.java`                                  | 修改     | 实现新增方法                                        |
| ClientCheckWebSocketManager            | `src/main/java/com/simple/ai/websocket/command/ClientCheckWebSocketManager.java`                       | 修改     | 注入 AgentClientView 替换 JdbcTemplate，全文件改造  |
| PageAggregateTaskRequest               | `src/main/java/com/simple/ai/common/dto/task/PageAggregateTaskRequest.java`                            | 修改     | 英文 → 中文注释                                     |
| PageAggregateTaskResponse              | `src/main/java/com/simple/ai/common/dto/task/PageAggregateTaskResponse.java`                           | 修改     | 英文 → 中文注释                                     |
| PageAggregateAgentMemoryDetailRequest  | `src/main/java/com/simple/ai/common/dto/agentMemoryDetail/PageAggregateAgentMemoryDetailRequest.java`  | 修改     | 英文 → 中文注释                                     |
| PageAggregateAgentMemoryDetailResponse | `src/main/java/com/simple/ai/common/dto/agentMemoryDetail/PageAggregateAgentMemoryDetailResponse.java` | 修改     | 英文 → 中文注释                                     |
| PageAggregateAgentMemoryResponse       | `src/main/java/com/simple/ai/common/dto/agentMemory/PageAggregateAgentMemoryResponse.java`             | 修改     | 英文 → 中文注释                                     |
| CreateAgentMemoryRequest               | `src/main/java/com/simple/ai/common/dto/agentMemory/CreateAgentMemoryRequest.java`                     | 修改     | 英文 → 中文注释                                     |
| CreateAgentMemoryDetailRequest         | `src/main/java/com/simple/ai/common/dto/agentMemoryDetail/CreateAgentMemoryDetailRequest.java`         | 修改     | 英文 → 中文注释                                     |

---

## 开发计划步骤

- [x] 步骤1: 修改 `AgentClientView` 接口，增加 `findByIdWithLock(String id)` 方法
- [x] 步骤2: 修改 `AgentClientRepository`，增加 `selectByIdWithLock(@Param("id") String id)` 方法；在 `AgentClientDao.xml` 增加 FOR UPDATE SQL
- [x] 步骤3: 修改 `MPAgentClientView`，实现新增的 View 方法
- [x] 步骤4: 改造 `ClientCheckWebSocketManager`，注入 `AgentClientView` 替换 `JdbcTemplate`，移除所有手写 SQL
- [x] 步骤5: 修改 `PageAggregateTaskRequest.java` 英文注释为中文
- [x] 步骤6: 修改 `PageAggregateTaskResponse.java` 英文注释为中文
- [x] 步骤7: 修改 `PageAggregateAgentMemoryDetailRequest.java` 英文注释为中文
- [x] 步骤8: 修改 `PageAggregateAgentMemoryDetailResponse.java` 英文注释为中文
- [x] 步骤9: 修改 `PageAggregateAgentMemoryResponse.java` 英文注释为中文
- [x] 步骤10: 修改 `CreateAgentMemoryRequest.java` 英文注释为中文（含 @NotEmpty 消息）
- [x] 步骤11: 修改 `CreateAgentMemoryDetailRequest.java` 英文注释为中文（含 @NotEmpty 消息）
- [x] 步骤12: 编译验证 `mvn clean compile` — BUILD SUCCESS
- [x] 步骤13: 深度自检

---

## 风险

1. `FOR UPDATE` 行锁：需要确保 MyBatis 自定义 SQL 正确传递参数，避免 SQL 注入
2. `AgentClient` 实体字段映射：`status` 字段使用 `AgentClientStatusProcess` 枚举，更新时需要正确设置枚举值
3. 编译依赖：`simple-common-mp` 已在 pom.xml 中存在，无需新增依赖

---

## 编译验证记录

- [x] `mvn clean compile` 通过 — BUILD SUCCESS（333 source files，0 error）

---

## 深度自检记录

- [x] 自检完成

### 自检结果

| 检查项                            | 状态 | 说明                                         |
|-----------------------------------|------|----------------------------------------------|
| 所有 public 类型和成员有 XML 注释 | ✓   | 新增的 findByIdWithLock 方法均有完整 Javadoc |
| 方法内部关键逻辑有自然语言注释    | ✓   | 所有分支、关键步骤均有中文注释               |
| 命名符合项目风格                  | ✓   | PascalCase/camelCase 与项目一致              |
| 共享状态线程安全                  | ✓   | FOR UPDATE 行锁保留，无竞态                  |
| 数据库查询无 N+1/无界/注入        | ✓   | 参数化查询，FOR UPDATE + WHERE 条件          |
| 资源已正确释放                    | ✓   | 无未释放资源                                 |
| 日志未泄漏敏感信息                | ✓   | 仅输出 cliKey，不输出 token/密钥             |
| mvn clean compile 通过            | ✓   | BUILD SUCCESS                                |
| JdbcTemplate 已全部移除           | ✓   | 项目中不再有 @Autowired JdbcTemplate         |
| DTO 注释已全部中文化              | ✓   | 7 个文件全部改为中文注释和校验消息           |
