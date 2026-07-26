# 技能返回格式 & 规则触发动作改为非必填

## 当前恢复入口

- 当前阶段：全部完成。
- 当前进行中：无
- 下一步：无

## 执行状态清单

- [x] 步骤1: 生成数据库变更 SQL（agent_skill.return_data_format 和 agent_rule.trigger_action 改为可选）
- [x] 步骤2: 修改 CreateAgentSkillRequest 去掉 returnDataFormat 的 @NotEmpty
- [x] 步骤3: 修改 CreateAgentRuleRequest 去掉 triggerAction 的 @NotEmpty
- [x] 步骤4: 修改 AgentToolRegistry 描述文字（必填→可选）
- [x] 步骤5: 前端 AgentSkillCreatePage 去掉 returnDataFormat 的 required 校验
- [x] 步骤6: 前端 AgentSkillEditPage 去掉 returnDataFormat 的 required 校验
- [x] 步骤7: 前端 AgentRuleCreatePage 去掉 triggerAction 的 required 校验
- [x] 步骤8: 前端 AgentRuleEditPage 去掉 triggerAction 的 required 校验
- [x] 步骤9: 后端编译验证 mvn clean package ✅
- [x] 步骤10: 前端编译验证 npm run build ✅
- [x] 步骤11: 深度自检（code-inspector + web-code-inspector）

## 整体设计思路

将技能表的 `return_data_format` 字段和规则表的 `trigger_action` 字段从必填改为可选。

- 数据库层：移除 NOT NULL 约束
- 后端 DTO：移除 @NotEmpty 校验注解
- 前端表单：移除 required: true 校验规则
- 不影响已有数据，不影响其他业务逻辑

## 变更摘要

### 数据库

- `agent_skill.return_data_format` → DROP NOT NULL
- `agent_rule.trigger_action` → DROP NOT NULL
- SQL 文件: `doc/sql/migration_optional_fields.sql`

### 后端

- `CreateAgentSkillRequest.java` → 去掉 returnDataFormat 的 `@NotEmpty`
- `CreateAgentRuleRequest.java` → 去掉 triggerAction 的 `@NotEmpty`
- `AgentToolRegistry.java` → 描述文字"必填"→"可选"

### 前端

- `AgentSkillCreatePage.tsx` → 去掉 returnDataFormat 的 `required: true`
- `AgentSkillEditPage.tsx` → 去掉 returnDataFormat 的 `required: true`
- `AgentRuleCreatePage.tsx` → 去掉 triggerAction 的 `required: true`
- `AgentRuleEditPage.tsx` → 去掉 triggerAction 的 `required: true`

## 重要文件索引

| 文件                    | 路径                                                          | 改动类型 | 说明                               |
|-------------------------|---------------------------------------------------------------|----------|------------------------------------|
| CreateAgentSkillRequest | src/main/java/.../dto/agentSkill/CreateAgentSkillRequest.java | 修改     | 去掉 returnDataFormat 的 @NotEmpty |
| CreateAgentRuleRequest  | src/main/java/.../dto/agentRule/CreateAgentRuleRequest.java   | 修改     | 去掉 triggerAction 的 @NotEmpty    |
| AgentToolRegistry       | src/main/java/.../service/agent/AgentToolRegistry.java        | 修改     | 更新描述文字                       |
| AgentSkillCreatePage    | web/src/pages/AgentSkillCreatePage.tsx                        | 修改     | 去掉 required                      |
| AgentSkillEditPage      | web/src/pages/AgentSkillEditPage.tsx                          | 修改     | 去掉 required                      |
| AgentRuleCreatePage     | web/src/pages/AgentRuleCreatePage.tsx                         | 修改     | 去掉 required                      |
| AgentRuleEditPage       | web/src/pages/AgentRuleEditPage.tsx                           | 修改     | 去掉 required                      |
| migration SQL           | doc/sql/migration_optional_fields.sql                         | 新增     | 数据库变更 SQL                     |

## 编译验证记录

- [x] mvn clean package -DskipTests ✅
- [x] npm run build ✅