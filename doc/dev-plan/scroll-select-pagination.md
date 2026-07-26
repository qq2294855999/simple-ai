# 下拉滚动分页加载 - 开发计划

## 当前总体状态

- 当前阶段：阶段三，代码实现完成。
- 当前进行中：执行 npm run build 验证。
- 下一步：编译通过后任务完成。

---

## 业务重点

将前端所有下拉选择（Select 组件）中 `size: 1000` 的一次性全量加载，改为滚动分页加载：

- 初始加载 20 条
- 用户滚动下拉面板到底部时自动加载下一页
- 追加到已有选项列表，避免重复请求
- 全部加载完成后停止请求

---

## 执行状态清单

- [x] 步骤1: 创建 `useScrollSelect.ts` 自定义 Hook
- [x] 步骤2: 修改 `CommandDispatchPage.tsx`（agents + skills 下拉）
- [x] 步骤3: 修改 `AtomicCommandManagementPage.tsx`（skills + executors 下拉）
- [x] 步骤4: 修改 `AgentDesignManagementPage.tsx`（rules + skills 下拉 × 2 组）
- [x] 步骤5: 修改 `AgentClientManagementPage.tsx`（executors 下拉）
- [x] 步骤6: 修改 `AgentChatPage.tsx`（agents + clients 下拉）
- [x] 步骤7: 清理 API 层硬编码 `size:1000` 的便捷方法
- [x] 步骤8: 修改 `TaskManagementPage.tsx`（agents 下拉）
- [x] 步骤9: 修改 `SubAgentRelationManagementPage.tsx`（agents 下拉 × 3）
- [x] 步骤10: 修改 `AgentSkillManagementPage.tsx`（agents 下拉）
- [x] 步骤11: 修改 `AgentSkillEditPage.tsx`（agents 下拉）
- [x] 步骤12: 修改 `AgentSkillCreatePage.tsx`（agents 下拉）
- [x] 步骤13: 修改 `AgentRuleManagementPage.tsx`（agents 下拉）
- [x] 步骤14: 修改 `AgentRuleEditPage.tsx`（agents 下拉）
- [x] 步骤15: 修改 `AgentRuleCreatePage.tsx`（agents 下拉）
- [x] 步骤16: 修改 `AgentMemoryManagementPage.tsx`（agents 下拉 × 2）
- [x] 步骤17: 修改 `AgentExecutorManagementPage.tsx`（protocols 下拉）
- [x] 步骤18: 执行 `npm run build` 验证

---

## 重要文件索引

| 文件                               | 路径                                             | 改动类型 | 说明                                          |
|------------------------------------|--------------------------------------------------|----------|-----------------------------------------------|
| useScrollSelect.ts                 | web/src/hooks/useScrollSelect.ts                 | 新增     | 滚动分页加载 Hook                             |
| CommandDispatchPage.tsx            | web/src/pages/CommandDispatchPage.tsx            | 修改     | agents + skills 下拉                          |
| AtomicCommandManagementPage.tsx    | web/src/pages/AtomicCommandManagementPage.tsx    | 修改     | skills + executors 下拉                       |
| AgentDesignManagementPage.tsx      | web/src/pages/AgentDesignManagementPage.tsx      | 修改     | rules + skills 下拉 × 2 组                    |
| AgentClientManagementPage.tsx      | web/src/pages/AgentClientManagementPage.tsx      | 修改     | executors 下拉                                |
| AgentChatPage.tsx                  | web/src/pages/AgentChatPage.tsx                  | 修改     | agents + clients 下拉                         |
| TaskManagementPage.tsx             | web/src/pages/TaskManagementPage.tsx             | 修改     | agents 下拉                                   |
| SubAgentRelationManagementPage.tsx | web/src/pages/SubAgentRelationManagementPage.tsx | 修改     | agents 下拉 × 3                               |
| AgentSkillManagementPage.tsx       | web/src/pages/AgentSkillManagementPage.tsx       | 修改     | agents 下拉                                   |
| AgentSkillEditPage.tsx             | web/src/pages/AgentSkillEditPage.tsx             | 修改     | agents 下拉                                   |
| AgentSkillCreatePage.tsx           | web/src/pages/AgentSkillCreatePage.tsx           | 修改     | agents 下拉                                   |
| AgentRuleManagementPage.tsx        | web/src/pages/AgentRuleManagementPage.tsx        | 修改     | agents 下拉                                   |
| AgentRuleEditPage.tsx              | web/src/pages/AgentRuleEditPage.tsx              | 修改     | agents 下拉                                   |
| AgentRuleCreatePage.tsx            | web/src/pages/AgentRuleCreatePage.tsx            | 修改     | agents 下拉                                   |
| AgentMemoryManagementPage.tsx      | web/src/pages/AgentMemoryManagementPage.tsx      | 修改     | agents 下拉 × 2                               |
| AgentExecutorManagementPage.tsx    | web/src/pages/AgentExecutorManagementPage.tsx    | 修改     | protocols 下拉                                |
| agentSkillApi.ts                   | web/src/api/agentSkillApi.ts                     | 修改     | 清理 listAll()                                |
| agentRuleApi.ts                    | web/src/api/agentRuleApi.ts                      | 修改     | 清理 listAll()                                |
| agentDefinitionApi.ts              | web/src/api/agentDefinitionApi.ts                | 修改     | 清理 listAll()                                |
| agentExecutorApi.ts                | web/src/api/agentExecutorApi.ts                  | 修改     | 新增 pageProtocols()，清理 findAllProtocols() |

---

## 构建验证记录

| 时间 | 结果 | 备注   |
|------|------|--------|
| -    | -    | 待执行 |

---

## 深度自检记录

| 时间 | 结果 | 备注   |
|------|------|--------|
| -    | -    | 待执行 |