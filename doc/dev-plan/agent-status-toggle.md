# 智能体/规则/技能启用禁用接口 — 开发计划

## 当前恢复入口

- **当前阶段**：阶段一，需求分析与计划输出。
- **当前进行中**：[-] 输出开发计划，准备进入阶段二（技术验证）。
- **下一步**：阶段二 — 验证所有引用类存在性、逻辑自洽性。

---

## 需求概述

为智能体定义（AgentDefinition）、智能体规则（AgentRule）、智能体技能（AgentSkill）三个模块追加「启用/禁用」切换接口（后端 Controller + Service），并在前端管理页面的表格操作列增加启用/禁用操作按钮。

## 技术栈

Java + Web（全栈）

---

## 业务流程图

```
用户点击「启用」或「禁用」按钮（前端页面）
  ↓
□ 调用后端 PUT /toggle/{id} 接口
  ↓
□ Controller 接收请求，校验 id 非空
  ↓
□ Service.toggleStatus(id) 方法：
  ├── 查询当前实体，校验存在性
  ├── 读取当前 status 值
  ├── ◇ 当前状态？
  │     ├── ON  → 设置为 OFF
  │     └── OFF → 设置为 ON
  └── 调用 View.updateById(entity) 持久化
  ↓
□ 返回 R.ok()
  ↓
□ 前端刷新列表，状态列显示新值
```

---

## 自检结论

### 已验证项

| 模块 | 已有内容 | 结论 |
|------|---------|------|
| Status 枚举 | `ON(1, "启用")`, `OFF(2, "禁用")` | 无需新增枚举值 ✅ |
| AgentDefinition Entity | 已有 `status` 字段，类型 `Status` | 无需修改 Entity ✅ |
| AgentRule Entity | 已有 `status` 字段，类型 `Status` | 无需修改 Entity ✅ |
| AgentSkill Entity | 已有 `status` 字段，类型 `Status` | 无需修改 Entity ✅ |
| PageAgentDefinitionResponse | 已有 `status` 字段 | 无需修改 DTO ✅ |
| PageAgentRuleResponse | 已有 `status` 字段 | 无需修改 DTO ✅ |
| PageAgentSkillResponse | 已有 `status` 字段 | 无需修改 DTO ✅ |
| 前端 AgentDesignManagementPage | 已有状态列（Tag），搜索区已有状态筛选 | 仅需加操作按钮 ✅ |
| 前端 AgentRuleManagementPage | 已有状态列，搜索区已有状态筛选 | 仅需加操作按钮 ✅ |
| 前端 AgentSkillManagementPage | 已有状态列，搜索区已有状态筛选 | 仅需加操作按钮 ✅ |
| DefaultAgentDefinitionService.save() | 创建时设 `Status.ON` | 无需修改 ✅ |

### 需要新增/修改

| 模块 | 改动类型 | 说明 |
|------|---------|------|
| AgentDefinitionService | 修改 | 新增 `toggleStatus(String id)` 方法声明 |
| DefaultAgentDefinitionService | 修改 | 实现 `toggleStatus` 方法 |
| AgentDefinitionController | 修改 | 新增 `PUT /toggle/{id}` 接口 |
| AgentRuleService | 修改 | 新增 `toggleStatus(String id)` 方法声明 |
| DefaultAgentRuleService | 修改 | 实现 `toggleStatus` 方法 |
| AgentRuleController | 修改 | 新增 `PUT /toggle/{id}` 接口 |
| AgentSkillService | 修改 | 新增 `toggleStatus(String id)` 方法声明 |
| DefaultAgentSkillService | 修改 | 实现 `toggleStatus` 方法 |
| AgentSkillController | 修改 | 新增 `PUT /toggle/{id}` 接口 |
| agentDefinitionApi.ts | 修改 | 新增 `toggle(id)` 方法 |
| agentRuleApi.ts | 修改 | 新增 `toggle(id)` 方法 |
| agentSkillApi.ts | 修改 | 新增 `toggle(id)` 方法 |
| AgentDesignManagementPage.tsx | 修改 | 操作列增加启用/禁用按钮 |
| AgentRuleManagementPage.tsx | 修改 | 操作列增加启用/禁用按钮；修复 getStatusLabel 用 ON/OFF 替代 ENABLE/DISABLE |
| AgentSkillManagementPage.tsx | 修改 | 操作列增加启用/禁用按钮；修复 getStatusLabel 用 ON/OFF 替代 ENABLE/DISABLE |

### 发现的 Bug

> 规则和技能页面的 `getStatusLabel` 函数使用 `"ENABLE"`/`"DISABLE"` 字符串匹配状态，但后端 `Status` 枚举值为 `ON(1)`/`OFF(2)`，JSON 序列化后为 `"ON"`/`"OFF"` 或数字。状态列当前显示为 `"-"`（永远匹配不到）。需要在本次改动中一并修正。

---

## 设计对齐缺口清单

| 状态 | 缺口描述 | 当前表现 | 后续处理 |
|------|---------|---------|---------|
| [x] | status 枚举值已存在 ON/OFF | 后端 Entity、DTO 均已完整 | 无需操作 |
| [ ] | 规则/技能前端 getStatusLabel 用错字面量 | 永远显示 "-" | 本次修正为 ON/OFF |

---

## 开发计划

### 后端（Java）

- [ ] 步骤1: 修改 [`AgentDefinitionService`](src/main/java/com/simple/ai/common/service/agentDefinition/AgentDefinitionService.java) — 新增 `toggleStatus(String id)` 方法声明
- [ ] 步骤2: 修改 [`DefaultAgentDefinitionService`](src/main/java/com/simple/ai/service/agentDefinition/DefaultAgentDefinitionService.java) — 实现 `toggleStatus` 方法（查询→取反→更新）
- [ ] 步骤3: 修改 [`AgentDefinitionController`](src/main/java/com/simple/ai/controller/agentDefinition/AgentDefinitionController.java) — 新增 `PUT /toggle/{id}` 接口
- [ ] 步骤4: 修改 [`AgentRuleService`](src/main/java/com/simple/ai/common/service/agentRule/AgentRuleService.java) — 新增 `toggleStatus(String id)` 方法声明
- [ ] 步骤5: 修改 [`DefaultAgentRuleService`](src/main/java/com/simple/ai/service/agentRule/DefaultAgentRuleService.java) — 实现 `toggleStatus` 方法
- [ ] 步骤6: 修改 [`AgentRuleController`](src/main/java/com/simple/ai/controller/agentRule/AgentRuleController.java) — 新增 `PUT /toggle/{id}` 接口
- [ ] 步骤7: 修改 [`AgentSkillService`](src/main/java/com/simple/ai/common/service/agentSkill/AgentSkillService.java) — 新增 `toggleStatus(String id)` 方法声明
- [ ] 步骤8: 修改 [`DefaultAgentSkillService`](src/main/java/com/simple/ai/service/agentSkill/DefaultAgentSkillService.java) — 实现 `toggleStatus` 方法
- [ ] 步骤9: 修改 [`AgentSkillController`](src/main/java/com/simple/ai/controller/agentSkill/AgentSkillController.java) — 新增 `PUT /toggle/{id}` 接口
- [ ] 步骤10: 执行 `mvn clean compile` 并确保通过

### 前端（Web）

- [ ] 步骤11: 修改 [`agentDefinitionApi.ts`](web/src/api/agentDefinitionApi.ts) — 新增 `toggle(id)` 方法
- [ ] 步骤12: 修改 [`agentRuleApi.ts`](web/src/api/agentRuleApi.ts) — 新增 `toggle(id)` 方法
- [ ] 步骤13: 修改 [`agentSkillApi.ts`](web/src/api/agentSkillApi.ts) — 新增 `toggle(id)` 方法
- [ ] 步骤14: 修改 [`AgentDesignManagementPage.tsx`](web/src/pages/AgentDesignManagementPage.tsx) — 操作列增加启用/禁用按钮（使用 Popconfirm）
- [ ] 步骤15: 修改 [`AgentRuleManagementPage.tsx`](web/src/pages/AgentRuleManagementPage.tsx) — 操作列增加启用/禁用按钮 + 修复 getStatusLabel
- [ ] 步骤16: 修改 [`AgentSkillManagementPage.tsx`](web/src/pages/AgentSkillManagementPage.tsx) — 操作列增加启用/禁用按钮 + 修复 getStatusLabel
- [ ] 步骤17: 执行 `npm run build` 并确保通过

### 终极自检

- [ ] 步骤18: 执行 code-inspector 深度自检

---

## 重点业务流程说明

1. **切换逻辑**：读取当前实体的 `status` 字段，若为 `ON` 则设为 `OFF`，若为 `OFF` 则设为 `ON`。使用 `View.updateById(entity)` 持久化（MyBatis-Plus 的 updateById）。
2. **接口风格**：`PUT /sys/agent-definition/toggle/{id}`，无 RequestBody，直接返回 `R.ok()`。
3. **权限注解**：沿用现有权限前缀，分别为 `sys:agent-definition:toggle`、`sys:agent-rule:toggle`、`sys:agent-skill:toggle`。
4. **前端按钮**：操作列新增一个链接按钮，显示「启用」（当前禁用时）或「禁用」（当前启用时），使用 Popconfirm 确认操作。

---

## 重要文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|------|------|---------|------|
| Status | simple-common-mp/.../Status.java | 不变 | 已有 ON/OFF |
| AgentDefinition | src/.../entity/agentDefinition/AgentDefinition.java | 不变 | 已有 status 字段 |
| AgentRule | src/.../entity/agentRule/AgentRule.java | 不变 | 已有 status 字段 |
| AgentSkill | src/.../entity/agentSkill/AgentSkill.java | 不变 | 已有 status 字段 |
| AgentDefinitionService | src/.../service/agentDefinition/AgentDefinitionService.java | 修改 | 新增方法声明 |
| DefaultAgentDefinitionService | src/.../service/agentDefinition/DefaultAgentDefinitionService.java | 修改 | 实现 toggle |
| AgentDefinitionController | src/.../controller/agentDefinition/AgentDefinitionController.java | 修改 | 新增接口 |
| AgentRuleService | src/.../service/agentRule/AgentRuleService.java | 修改 | 新增方法声明 |
| DefaultAgentRuleService | src/.../service/agentRule/DefaultAgentRuleService.java | 修改 | 实现 toggle |
| AgentRuleController | src/.../controller/agentRule/AgentRuleController.java | 修改 | 新增接口 |
| AgentSkillService | src/.../service/agentSkill/AgentSkillService.java | 修改 | 新增方法声明 |
| DefaultAgentSkillService | src/.../service/agentSkill/DefaultAgentSkillService.java | 修改 | 实现 toggle |
| AgentSkillController | src/.../controller/agentSkill/AgentSkillController.java | 修改 | 新增接口 |
| agentDefinitionApi.ts | web/src/api/agentDefinitionApi.ts | 修改 | 新增 toggle |
| agentRuleApi.ts | web/src/api/agentRuleApi.ts | 修改 | 新增 toggle |
| agentSkillApi.ts | web/src/api/agentSkillApi.ts | 修改 | 新增 toggle |
| AgentDesignManagementPage | web/src/pages/AgentDesignManagementPage.tsx | 修改 | 加操作按钮 |
| AgentRuleManagementPage | web/src/pages/AgentRuleManagementPage.tsx | 修改 | 加按钮+修bug |
| AgentSkillManagementPage | web/src/pages/AgentSkillManagementPage.tsx | 修改 | 加按钮+修bug |

---

## 编译验证记录

（待阶段三完成后填写）

## 深度自检记录

（待阶段五完成后填写）
