# 技能/规则/智能体定义 MD 化改造 开发计划

> 状态: 全部完成
> 作者: qty
> 日期: 2026-07-26

## 当前恢复入口

- 当前阶段：全部完成，无待处理步骤。

## 全局约束与依赖

- 编译命令: `mvn clean package`（Java）、`npm run build`（Web）
- 代码规范: java开发规范.md、web代码编辑规范.md
- 前端框架: React 18 + TypeScript + Ant Design 5 + @uiw/react-md-editor
- 后端框架: Spring Boot + MyBatis-Plus + PostgreSQL
- 参考实现: Protocol 模块（完整的 MD CRUD 模式）

## 需求概览

### 核心目标

将技能（AgentSkill）、规则（AgentRule）、智能体定义（AgentDefinition）的增/改/查从 Modal 弹窗模式完整替换为协议的 MD 独立页面模式：

1. **新增**：跳转独立 CreatePage（元信息表单 + MDEditor）
2. **编辑**：跳转独立 EditPage（元信息表单 + MDEditor，回显已有数据）
3. **查看**：跳转独立 DetailPage（Descriptions + RestrictedMarkdownComponent 渲染 MD）
4. **启用/停用**：toggle-status API（与 Protocol 一致）

### 参考实现

Protocol 模块已完整实现此模式：

- 后端: ProtocolController → ProtocolService → ProtocolView → ProtocolRepository
- 前端: ProtocolManagementPage → ProtocolCreatePage → ProtocolEditPage → ProtocolDetailPage
- 关键组件: MDEditor（编辑）、RestrictedMarkdownComponent（渲染）
- 数据字段: `content` TEXT 存储完整 MD 文档

## 业务流程图

```
用户操作
  ↓
□ 列表页（ManagementPage）
  ├── 点击"新增" → 跳转 CreatePage
  │     ├── 上方：元信息表单（agentId, definitionDesc, status 等）
  │     ├── 下方：MDEditor 编辑 content
  │     └── 底部：取消 / 保存
  ├── 点击"编辑" → 跳转 EditPage
  │     ├── 上方：元信息表单（回显）
  │     ├── 下方：MDEditor 编辑 content（回显）
  │     └── 底部：取消 / 保存
  ├── 点击"查看" → 跳转 DetailPage
  │     ├── 上方：Descriptions 展示元信息
  │     ├── 下方：RestrictedMarkdownComponent 渲染 content
  │     └── 右上角：编辑按钮
  └── 点击"更多>启用/停用" → toggle-status API
```

## 设计决策

| 决策                               | 结论                                          | 理由                                                                                          |
|------------------------------------|-----------------------------------------------|-----------------------------------------------------------------------------------------------|
| content 与现有字段关系             | 新增 content TEXT，保留现有 structured fields | AgentContextAssembler 运行时使用 structured fields 注入 AI prompt，content 是面向人的 MD 文档 |
| 列表页是否显示 content             | 不显示                                        | 与 Protocol 一致，列表只显示元信息，content 在 DetailPage 查看                                |
| AgentDesignManagementPage 行内弹窗 | 改为导航到独立管理页面                        | 与 Protocol 模式一致，避免弹窗内嵌入 MDEditor 交互复杂                                        |
| toggle-status vs enable/disable    | 新增 toggle-status，保留 enable/disable       | 兼容现有 AI 工具调用（AgentToolRegistry）                                                     |
| AgentSkill 的 content 内容         | 技能的完整定义和执行内容（MD 格式）           | 替代原有的 execContent 纯文本，支持富格式描述                                                 |
| AgentRule 的 content 内容          | 规则的完整定义（MD 格式）                     | 替代原有的 triggerCondition + triggerAction 纯文本                                            |
| AgentDefinition 的 content 内容    | 智能体的完整定义（MD 格式）                   | 替代原有的 firstPrinciple + secondRule + thirdSkill 纯文本                                    |

## CRUD 契约表

### AgentSkill

| 维度               | 内容                                                                                             | 验证状态        |
|--------------------|--------------------------------------------------------------------------------------------------|-----------------|
| 接口路径           | GET sys/agent-skill/aggregate-list（修改响应字段）                                               | ✅ 已存在       |
| 接口路径           | GET sys/agent-skill/find/{id}（修改响应字段，含 content）                                        | ✅ 已存在       |
| 接口路径           | POST sys/agent-skill/create（修改请求字段，含 content）                                          | ✅ 已存在       |
| 接口路径           | PUT sys/agent-skill/update/{id}（修改请求字段，含 content）                                      | ✅ 已存在       |
| 接口路径           | PUT sys/agent-skill/toggle-status/{id}（新增）                                                   | ✅ 待创建       |
| Request DTO 白名单 | CreateAgentSkillRequest: agentId, definitionDesc, execContent, returnDataFormat, content, remark | ✅ 新增 content |
| Response DTO       | InfoAgentSkillResponse 新增 content；PageAggregate 不含 content                                  | ✅              |

### AgentRule

| 维度               | 内容                                                                                              | 验证状态        |
|--------------------|---------------------------------------------------------------------------------------------------|-----------------|
| 接口路径           | GET sys/agent-rule/aggregate-list（修改响应字段）                                                 | ✅ 已存在       |
| 接口路径           | GET sys/agent-rule/find/{id}（修改响应字段，含 content）                                          | ✅ 已存在       |
| 接口路径           | POST sys/agent-rule/create（修改请求字段，含 content）                                            | ✅ 已存在       |
| 接口路径           | PUT sys/agent-rule/update/{id}（修改请求字段，含 content）                                        | ✅ 已存在       |
| 接口路径           | PUT sys/agent-rule/toggle-status/{id}（新增）                                                     | ✅ 待创建       |
| Request DTO 白名单 | CreateAgentRuleRequest: agentId, definitionDesc, triggerCondition, triggerAction, content, remark | ✅ 新增 content |
| Response DTO       | InfoAgentRuleResponse 新增 content；PageAggregate 不含 content                                    | ✅              |

### AgentDefinition

| 维度               | 内容                                                                                                                        | 验证状态        |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------|-----------------|
| 接口路径           | GET sys/agent-definition/aggregate-list（修改响应字段）                                                                     | ✅ 已存在       |
| 接口路径           | GET sys/agent-definition/find/{id}（修改响应字段，含 content）                                                              | ✅ 已存在       |
| 接口路径           | POST sys/agent-definition/create（修改请求字段，含 content）                                                                | ✅ 已存在       |
| 接口路径           | PUT sys/agent-definition/update/{id}（修改请求字段，含 content）                                                            | ✅ 已存在       |
| 接口路径           | PUT sys/agent-definition/toggle-status/{id}（新增）                                                                         | ✅ 待创建       |
| Request DTO 白名单 | CreateAgentDefinitionRequest: name, definitionDesc, firstPrinciple, secondRule, thirdSkill, defaultModelId, content, remark | ✅ 新增 content |
| Response DTO       | InfoAgentDefinitionResponse 新增 content；PageAggregate 不含 content                                                        | ✅              |

## 设计对齐缺口清单

| 状态 | 设计要点                           | 当前代码表现                | 后续处理                     |
|------|------------------------------------|-----------------------------|------------------------------|
| [ ]  | agent_skill.content 字段           | 不存在                      | ALTER TABLE ADD content TEXT |
| [ ]  | agent_rule.content 字段            | 不存在                      | ALTER TABLE ADD content TEXT |
| [ ]  | agent_definition.content 字段      | 不存在                      | ALTER TABLE ADD content TEXT |
| [ ]  | AgentSkill toggle-status           | 不存在                      | Controller + Service 新增    |
| [ ]  | AgentRule toggle-status            | 不存在                      | Controller + Service 新增    |
| [ ]  | AgentDefinition toggle-status      | 不存在（有 enable/disable） | Controller + Service 新增    |
| [ ]  | AgentDesignManagementPage 行内弹窗 | 使用 Modal 管理 Rule/Skill  | 改为导航到独立管理页面       |

## 重要文件索引

| 文件                               | 路径                                                               | 改动类型 | 说明                                       |
|------------------------------------|--------------------------------------------------------------------|----------|--------------------------------------------|
| migration_md_content.sql           | doc/sql/migration_md_content.sql                                   | 新增     | 3 表 ALTER ADD content                     |
| AgentSkill.java                    | src/.../entity/agentSkill/AgentSkill.java                          | 修改     | 新增 content 字段                          |
| AgentRule.java                     | src/.../entity/agentRule/AgentRule.java                            | 修改     | 新增 content 字段                          |
| AgentDefinition.java               | src/.../entity/agentDefinition/AgentDefinition.java                | 修改     | 新增 content 字段                          |
| CreateAgentSkillRequest.java       | src/.../dto/agentSkill/CreateAgentSkillRequest.java                | 修改     | 新增 content                               |
| UpdateAgentSkillRequest.java       | src/.../dto/agentSkill/UpdateAgentSkillRequest.java                | 修改     | 继承自 Create，自动含 content              |
| InfoAgentSkillResponse.java        | src/.../dto/agentSkill/InfoAgentSkillResponse.java                 | 修改     | 新增 content                               |
| CreateAgentRuleRequest.java        | src/.../dto/agentRule/CreateAgentRuleRequest.java                  | 修改     | 新增 content                               |
| UpdateAgentRuleRequest.java        | src/.../dto/agentRule/UpdateAgentRuleRequest.java                  | 修改     | 继承自 Create，自动含 content              |
| InfoAgentRuleResponse.java         | src/.../dto/agentRule/InfoAgentRuleResponse.java                   | 修改     | 新增 content                               |
| CreateAgentDefinitionRequest.java  | src/.../dto/agentDefinition/CreateAgentDefinitionRequest.java      | 修改     | 新增 content                               |
| UpdateAgentDefinitionRequest.java  | src/.../dto/agentDefinition/UpdateAgentDefinitionRequest.java      | 修改     | 继承自 Create，自动含 content              |
| InfoAgentDefinitionResponse.java   | src/.../dto/agentDefinition/InfoAgentDefinitionResponse.java       | 修改     | 新增 content                               |
| AgentSkillController.java          | src/.../controller/agentSkill/AgentSkillController.java            | 修改     | 新增 toggle-status                         |
| AgentRuleController.java           | src/.../controller/agentRule/AgentRuleController.java              | 修改     | 新增 toggle-status                         |
| AgentDefinitionController.java     | src/.../controller/agentDefinition/AgentDefinitionController.java  | 修改     | 新增 toggle-status                         |
| AgentSkillService.java             | src/.../service/agentSkill/AgentSkillService.java                  | 修改     | 新增 toggleStatus                          |
| DefaultAgentSkillService.java      | src/.../service/agentSkill/DefaultAgentSkillService.java           | 修改     | 实现 toggleStatus                          |
| AgentRuleService.java              | src/.../service/agentRule/AgentRuleService.java                    | 修改     | 新增 toggleStatus                          |
| DefaultAgentRuleService.java       | src/.../service/agentRule/DefaultAgentRuleService.java             | 修改     | 实现 toggleStatus                          |
| AgentDefinitionService.java        | src/.../service/agentDefinition/AgentDefinitionService.java        | 修改     | 新增 toggleStatus                          |
| DefaultAgentDefinitionService.java | src/.../service/agentDefinition/DefaultAgentDefinitionService.java | 修改     | 实现 toggleStatus                          |
| AgentSkillDto.ts                   | web/src/dto/agentSkill/CreateAgentSkillRequestDto.ts               | 修改     | 新增 content                               |
| AgentSkillPageResponseDto.ts       | web/src/dto/agentSkill/AgentSkillPageResponseDto.ts                | 不变     | 列表不含 content                           |
| AgentRuleDto.ts                    | web/src/dto/agentRule/AgentRuleDto.ts                              | 修改     | 新增 content                               |
| AgentDefinitionDto.ts              | web/src/dto/agentDefinition/AgentDefinitionDto.ts                  | 修改     | 新增 content                               |
| agentSkillApi.ts                   | web/src/api/agentSkillApi.ts                                       | 修改     | 新增 toggleStatus                          |
| agentRuleApi.ts                    | web/src/api/agentRuleApi.ts                                        | 修改     | 新增 toggleStatus                          |
| agentDefinitionApi.ts              | web/src/api/agentDefinitionApi.ts                                  | 修改     | 新增 toggleStatus                          |
| AgentSkillCreatePage.tsx           | web/src/pages/AgentSkillCreatePage.tsx                             | 新增     | 元信息表单 + MDEditor                      |
| AgentSkillEditPage.tsx             | web/src/pages/AgentSkillEditPage.tsx                               | 新增     | 元信息表单 + MDEditor（回显）              |
| AgentSkillDetailPage.tsx           | web/src/pages/AgentSkillDetailPage.tsx                             | 新增     | Descriptions + RestrictedMarkdownComponent |
| AgentSkillManagementPage.tsx       | web/src/pages/AgentSkillManagementPage.tsx                         | 修改     | 移除 Modal，改为导航                       |
| AgentRuleCreatePage.tsx            | web/src/pages/AgentRuleCreatePage.tsx                              | 新增     | 元信息表单 + MDEditor                      |
| AgentRuleEditPage.tsx              | web/src/pages/AgentRuleEditPage.tsx                                | 新增     | 元信息表单 + MDEditor（回显）              |
| AgentRuleDetailPage.tsx            | web/src/pages/AgentRuleDetailPage.tsx                              | 新增     | Descriptions + RestrictedMarkdownComponent |
| AgentRuleManagementPage.tsx        | web/src/pages/AgentRuleManagementPage.tsx                          | 修改     | 移除 Modal，改为导航                       |
| AgentDefinitionCreatePage.tsx      | web/src/pages/AgentDefinitionCreatePage.tsx                        | 新增     | 元信息表单 + MDEditor                      |
| AgentDefinitionEditPage.tsx        | web/src/pages/AgentDefinitionEditPage.tsx                          | 新增     | 元信息表单 + MDEditor（回显）              |
| AgentDefinitionDetailPage.tsx      | web/src/pages/AgentDefinitionDetailPage.tsx                        | 新增     | Descriptions + RestrictedMarkdownComponent |
| AgentDesignManagementPage.tsx      | web/src/pages/AgentDesignManagementPage.tsx                        | 修改     | 行内弹窗改为导航                           |
| AppRouter.tsx                      | web/src/router/AppRouter.tsx                                       | 修改     | 新增 9 条路由                              |

## 执行状态清单

### 阶段零：数据库变更

- [x] 步骤0: 使用 db-ddl-generator 生成 3 表 ALTER SQL（ADD content TEXT），写入 doc/sql/migration_md_content.sql

### 阶段一：Java 后端编码

- [x] 步骤1: 修改 AgentSkill Entity（新增 content 字段）
- [x] 步骤2: 修改 AgentRule Entity（新增 content 字段）
- [x] 步骤3: 修改 AgentDefinition Entity（新增 content 字段）
- [x] 步骤4: 修改 AgentSkill DTO（CreateRequest + InfoResponse 新增 content）
- [x] 步骤5: 修改 AgentRule DTO（CreateRequest + InfoResponse 新增 content）
- [x] 步骤6: 修改 AgentDefinition DTO（CreateRequest + InfoResponse 新增 content）
- [x] 步骤7: 修改 AgentSkillController（新增 toggle-status 端点）
- [x] 步骤8: 修改 AgentRuleController（新增 toggle-status 端点）
- [x] 步骤9: 修改 AgentDefinitionController（新增 toggle-status 端点）
- [x] 步骤10: 修改 AgentSkillService + DefaultAgentSkillService（新增 toggleStatus）
- [x] 步骤11: 修改 AgentRuleService + DefaultAgentRuleService（新增 toggleStatus）
- [x] 步骤12: 修改 AgentDefinitionService + DefaultAgentDefinitionService（新增 toggleStatus）
- [x] 步骤13: mvn clean package 编译验证

### 阶段二：Web 前端编码

- [x] 步骤14: 修改 AgentSkill DTO（CreateAgentSkillRequestDto + AgentSkillInfoResponseDto 新增 content）
- [x] 步骤15: 修改 AgentRule DTO（AgentRuleDto 新增 content + AgentRuleInfoResponseDto）
- [x] 步骤16: 修改 AgentDefinition DTO（AgentDefinitionDto 新增 content）
- [x] 步骤17: 修改 agentSkillApi.ts（新增 toggleStatus + findOne 返回 InfoResponse）
- [x] 步骤18: 修改 agentRuleApi.ts（新增 toggleStatus + findOne 返回 InfoResponse）
- [x] 步骤19: 修改 agentDefinitionApi.ts（新增 toggleStatus）
- [x] 步骤20: 新增 AgentSkillCreatePage.tsx（元信息表单 + MDEditor）
- [x] 步骤21: 新增 AgentSkillEditPage.tsx（元信息表单 + MDEditor 回显）
- [x] 步骤22: 新增 AgentSkillDetailPage.tsx（Descriptions + RestrictedMarkdownComponent）
- [x] 步骤23: 修改 AgentSkillManagementPage.tsx（移除 Modal，改为导航链接 + toggleStatus）
- [x] 步骤24: 新增 AgentRuleCreatePage.tsx（元信息表单 + MDEditor）
- [x] 步骤25: 新增 AgentRuleEditPage.tsx（元信息表单 + MDEditor 回显）
- [x] 步骤26: 新增 AgentRuleDetailPage.tsx（Descriptions + RestrictedMarkdownComponent）
- [x] 步骤27: 修改 AgentRuleManagementPage.tsx（移除 Modal，改为导航链接 + toggleStatus）
- [x] 步骤28: 新增 AgentDefinitionCreatePage.tsx（元信息表单 + MDEditor）
- [x] 步骤29: 新增 AgentDefinitionEditPage.tsx（元信息表单 + MDEditor 回显）
- [x] 步骤30: 新增 AgentDefinitionDetailPage.tsx（Descriptions + RestrictedMarkdownComponent）
- [x] 步骤31: 修改 AgentDesignManagementPage.tsx（移除创建/编辑弹窗，改为导航；规则/技能弹窗内编辑改为导航）
- [x] 步骤32: 修改 AppRouter.tsx（新增 9 条路由）
- [x] 步骤33: npm run build 编译验证

### 阶段三：编译+自检

- [x] 步骤34: 最终 mvn clean package + npm run build
- [x] 步骤35: code-inspector 深度自检

## 深度自检记录

### 一票否决项检查

| 检查项               | 结果 | 说明                                                                            |
|----------------------|------|---------------------------------------------------------------------------------|
| 表头强制不换行       | ✅   | 全局 CSS 已有 `.ant-table-thead > tr > th { white-space: nowrap; }`             |
| 状态列 Tag 颜色      | ✅   | 所有管理页/详情页均使用 `<Tag color={green/red}>`，通过 `getStatusLabel()` 判断 |
| 操作列更多 Dropdown  | ✅   | 编辑外露 + 更多下拉，删除 `danger: true`                                        |
| 备注列截断 + Tooltip | ✅   | `ellipsis: true` + `<Tooltip>` + `v \|\| "-"`                                   |
| 防重复提交           | ✅   | 所有提交按钮使用 `usePreventDoubleClickHook` + `loading`                        |

### 常规检查

| 检查项                                 | 结果 |
|----------------------------------------|------|
| 表格 bordered + rowKey="id"            | ✅   |
| 分页 showSizeChanger + showTotal       | ✅   |
| 长文本 ellipsis + Tooltip              | ✅   |
| 关联数据列显示名称                     | ✅   |
| 搜索面板 className + Space wrap + 36px | ✅   |
| 工具栏 className + 左批量右新增        | ✅   |
| 按钮类型正确                           | ✅   |
| ToastUtil 统一使用                     | ✅   |
| 删除 Popconfirm + okButtonProps danger | ✅   |
| JSDoc + @author qty                    | ✅   |
| useCallback / useMemo 优化             | ✅   |
| 无链式调用超过 2 个 .                  | ✅   |

### 关键修复

1. **删除操作缺少 Popconfirm 确认**：AgentSkillManagementPage、AgentRuleManagementPage、AgentDesignManagementPage 的 Dropdown 菜单中删除操作未使用 Popconfirm，已修复为在
   `label` 中嵌入 `<Popconfirm>` + `okButtonProps={{danger: true}}`，删除逻辑由 `onConfirm` 处理