# 人机会话实时进度面板开发计划

## 当前恢复入口

- 当前阶段：阶段三，代码实现。
- 当前进行中：[-] 修改 agentChatStreamUtil.ts，新增进度事件转换工具函数。
- 下一步：修改 AgentChatPage.tsx，新增进度事件收集状态和实时进度面板渲染。

## 执行状态清单

- [x] 需求分析与方案设计（用户确认）
- [x] 修改 agentChatStreamUtil.ts，新增 progressEventsToExecutionEvents 工具函数
- [x] 修改 AgentChatPage.tsx，新增 progressEvents 状态
- [x] 修改 AgentChatPage.tsx，修改 handleProgress 收集进度事件
- [x] 修改 AgentChatPage.tsx，新增实时进度面板渲染
- [x] 执行 npm run build
- [x] 执行 web-code-inspector 深度自检
- [x] 修复 execution_event/chat_turn/memory_evidence 表 status 字段类型不匹配问题

## Bug 修复记录

### execution_event 表 status 字段类型不匹配

**错误现象**：`conversion to class java.lang.Integer from varchar not supported`

**根因**：

- `execution_event`、`chat_turn`、`memory_evidence` 表的 `status` 字段是 `varchar(32)`，存储值为 `'ON'`
- Java 实体 `ExecutionEvent.java` 的 `status` 字段类型是 `Status` 枚举
- MyBatis-Plus 的 `MybatisEnumTypeHandler` 默认按枚举的 `code`（Integer）进行映射
- 数据库中存的是字符串 `'ON'`，但枚举处理器期望的是整数 `1`

**修复方案**：

- 将 `execution_event`、`chat_turn`、`memory_evidence` 表的 `status` 字段从 `varchar(32)` 改为 `int2`
- 将 INSERT 语句中的 `'1'` 改为 `1`
- 与其他表（如 `task`、`task_detail`、`atomic_command` 等）保持一致

**影响范围验证**：

- `agent_memory_version_detail` 表使用 `String` 类型，不受影响
- `atomic_command` 表已使用 `int2`，无需修改
- 所有使用 `Status` 枚举的实体均已验证

## 深度自检记录

### 一票否决项检查

| 序号 | 检查项             | 结果 | 说明                                           |
|------|--------------------|------|------------------------------------------------|
| 1    | 表头强制不换行     | 通过 | 本页面为对话页，不涉及 Table 组件              |
| 2    | 状态列 Tag 颜色    | 通过 | 进度事件使用 Tag color 标记（red/blue/purple） |
| 3    | 操作列更多下拉     | 通过 | 本页面为对话页，不涉及操作列                   |
| 4    | 备注列截断+Tooltip | 通过 | 进度事件 payload 使用 Tooltip 截断展示         |
| 5    | 防重复提交         | 通过 | 发送按钮使用 usePreventDoubleClickHook         |

### Web 规则 Checklist

| 检查项                   | 结果 | 说明                          |
|--------------------------|------|-------------------------------|
| 反馈统一使用 ToastUtil   | 通过 | 错误提示使用 ToastUtil.error  |
| 删除确认使用 Popconfirm  | 通过 | 会话删除使用 Popconfirm       |
| JSDoc 注释 + @author     | 通过 | 组件有标准 JSDoc              |
| 重要步骤有换行注释       | 通过 | handleProgress 每个分支有注释 |
| 使用 useCallback/useMemo | 通过 | 所有回调使用 useCallback      |
| 无链式调用超过 2 个      | 通过 | 代码风格合规                  |
| 无未使用的 import        | 通过 | 已清理                        |

### 关键修复内容

- 修复 handleProgress 闭包问题：使用 useRef 存储 progressEvents 快照，避免 SSE 回调引用不稳定
- 新增 progressEventsToExecutionEvents 工具函数，统一进度事件转执行事件逻辑
- 新增实时进度面板，使用 Collapse 组件展示执行过程
- 消息完成时自动将进度事件附加到最终消息的 executionEvents

## 重点业务流程说明

### 数据流

```
后端 publishProgress → SSE 推送 CommandDispatchProgressEvent → 前端 handleProgress 收集 → 实时渲染可折叠面板 → 消息完成时转为 executionEvents 附加到最终消息
```

### 进度事件类型

- CONTEXT_ASSEMBLING / CONTEXT_ASSEMBLED — 智能体上下文装配
- RULE_LOADED / SKILL_LOADED / SUB_AGENT_LOADED — 规则/技能/子智能体加载
- MEMORY_MATCHING / MEMORY_MATCHED / MEMORY_MISSED — 记忆匹配
- AI_STARTED / AI_COMPLETED — AI 调用
- STEP_STARTED / STEP_COMPLETED — 记忆步骤执行
- SUB_AGENT_STARTED / SUB_AGENT_COMPLETED — 子智能体调度
- AI_TOKEN — AI 输出 token（仅进入对话消息流）
- MESSAGE_COMPLETED / CHAT_FAILED — 消息完成/失败

### 展示规则

- token 事件仅进入对话消息流，不展示为进度
- 非 token 事件实时收集，展示为可折叠面板
- 消息完成时，进度事件转为 executionEvents 附加到最终消息
- 刷新页面后，executionEvents 从后端查询展示

## 设计对齐缺口清单

| 状态 | 设计要点          | 当前表现                    | 后续处理                          |
|------|-------------------|-----------------------------|-----------------------------------|
| [x]  | Markdown 表格渲染 | react-markdown + remark-gfm | 保持当前实现，AI 生成规范表格即可 |
| [x]  | 进度事件 SSE 推送 | 后端已完整实现              | 前端收集展示即可                  |
| [x]  | 可折叠面板        | Ant Design Collapse         | 使用 Collapse 组件实现            |
| [x]  | 实时展示          | SSE 流式推送                | handleProgress 收集非 token 事件  |

## 重要文件索引表

| 文件                   | 路径                                 | 改动类型 | 说明                                                            |
|------------------------|--------------------------------------|----------|-----------------------------------------------------------------|
| agentChatStreamUtil.ts | web/src/utils/agentChatStreamUtil.ts | 修改     | 新增 progressEventsToExecutionEvents 工具函数                   |
| AgentChatPage.tsx      | web/src/pages/AgentChatPage.tsx      | 修改     | 新增进度事件收集状态、修改 handleProgress、新增实时进度面板渲染 |

## 后端改动

**零改动**。完全复用已有的 CommandDispatchProgressEvent SSE 推送机制。