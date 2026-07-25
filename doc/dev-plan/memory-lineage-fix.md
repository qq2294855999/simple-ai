# 记忆血脉追溯与校验逻辑修复

## 当前恢复入口

- 当前阶段：阶段一，需求分析与计划输出
- 当前进行中：[-] 制定开发计划
- 下一步：执行 Java 后端修复

## 整体设计思路

### 核心哲学

记忆是智能体探索成功路径的沉淀。版本号应在 **同一逻辑记忆的血脉内**递增，而非全局递增。修订产生的新版本必须能追溯到旧版本，形成版本链。

### 关键角色关系

- `AgentMemory`：记忆主表，新增 `parentMemoryId` 字段建立版本链
- `DefaultMemoryDistiller`：蒸馏器，修复版本号计算逻辑
- `DefaultMemoryExecutor`：执行器，新增防御性校验
- `DefaultAgentMemoryService`：服务层，修复冗余更新和参数校验

### 设计决策记录

1. **版本号计算改为基于旧记忆递增**：修订场景 `oldMemory.versionNo + 1`，首次探索固定为 1
2. **新增 `parent_memory_id` 字段**：建立记忆版本链，支持前端展示版本历史
3. **Executor 增加防御性校验**：即使调用方已校验，Executor 自身也校验 versionStatus
4. **移除 Executor 中的冗余 Task 更新**：`memoryId`/`memoryVersionNo` 由调用方一次性设置

## 业务流程图

```
用户选择记忆+参数
  ↓
□ Controller.execute()
  ↓
□ Service.execute() [校验PUBLISHED + 参数校验 + 创建Task(含memoryId/memoryVersionNo)]
  ↓
□ MemoryExecutor.execute() [防御性校验versionStatus + 加载步骤 + 替换占位符 + 执行]
  ├── 全部成功 → task.SUCCESS
  └── 失败 → triggerMemoryRevision()
        ↓
      □ MemoryDistiller.distill() [修订场景]
        ├── 退役旧记忆(versionStatus=3)
        ├── 设置parentMemoryId=旧记忆ID
        ├── 版本号=旧记忆.versionNo+1
        └── 创建新记忆(DRAFT) + 步骤
```

## API 接口设计

### 接口总览表

| 接口         | 方法   | 路径                                    | 说明                           |
|--------------|--------|-----------------------------------------|--------------------------------|
| 分页查询     | GET    | sys/agent-memory/list                   | 已有                           |
| 查询详情     | GET    | sys/agent-memory/find/{id}              | 已有，新增 parentMemoryId 字段 |
| 创建         | POST   | sys/agent-memory/create                 | 已有                           |
| 更新         | PUT    | sys/agent-memory/update/{id}            | 已有                           |
| 删除         | DELETE | sys/agent-memory/deletes                | 已有                           |
| 发布         | PUT    | sys/agent-memory/publish/{id}           | 已有                           |
| 退役         | PUT    | sys/agent-memory/retire/{id}            | 已有                           |
| 参数定义     | GET    | sys/agent-memory/{id}/params-definition | 已有                           |
| 执行         | POST   | sys/agent-memory/{id}/execute           | 已有                           |
| **版本历史** | GET    | sys/agent-memory/{id}/version-history   | **新增**                       |

### 核心接口详细 Request/Response

#### 版本历史接口

**Request**: GET /sys/agent-memory/{id}/version-history

**Response**: `R<List<MemoryVersionHistoryResponse>>`

```java
MemoryVersionHistoryResponse {
    String id;           // 记忆ID
    Integer versionNo;   // 版本号
    Integer versionStatus; // 版本状态
    String parentMemoryId; // 父记忆ID
    String createReason;   // 创建原因
    String summary;        // 摘要
    Date createTime;       // 创建时间
}
```

## 管理后台页面设计

### 记忆编排管理页（AgentMemoryManagementPage）

```
┌──────────────────────────────────────────────────────────────┐
│ 搜索栏：[智能体下拉] [版本状态] [关键字] [搜索] [重置]       │
├──────────────────────────────────────────────────────────────┤
│ 工具栏：[+新建] [批量删除]                                    │
├──────────────────────────────────────────────────────────────┤
│ 表格列：                                                     │
│ 智能体名称 | 记忆名称 | 版本号 | 版本状态 | 摘要 |           │
│ 创建原因 | 步骤数 | 参数数 | 修改时间 | 操作                 │
│                                                              │
│ 版本状态列：DRAFT=蓝Tag / PUBLISHED=绿Tag / RETIRED=灰Tag    │
│ 操作列：[更多▼] → 发布/退役/执行/版本历史/编辑/删除          │
└──────────────────────────────────────────────────────────────┘
```

### 记忆版本历史页（AgentMemoryVersionPage）

```
┌──────────────────────────────────────────────────────────────┐
│ 面包屑：记忆编排管理 > 版本历史                               │
├──────────────────────────────────────────────────────────────┤
│ 版本链时间线（垂直）：                                        │
│                                                              │
│  ● v3  PUBLISHED  MEMORY_REVISE  2026-07-25  [查看]         │
│  │                                                           │
│  ● v2  RETIRED    MEMORY_REVISE  2026-07-24  [查看]         │
│  │                                                           │
│  ● v1  RETIRED    AI_EXPLORATION 2026-07-23  [查看]         │
│                                                              │
│  点击[查看] → 弹窗显示该版本记忆详情+步骤列表                │
└──────────────────────────────────────────────────────────────┘
```

### 执行弹窗

```
┌──────────────────────────────────────────────────────────────┐
│ 执行记忆：{记忆名称}                                         │
├──────────────────────────────────────────────────────────────┤
│ 版本：v{versionNo}  状态：PUBLISHED                          │
│                                                              │
│ 参数表单（动态生成）：                                       │
│ {param1}：[________]  *必填                                  │
│ {param2}：[________]                                        │
│                                                              │
│ 客户端：[下拉选择]                                           │
│                                                              │
│ [取消]  [执行]                                               │
└──────────────────────────────────────────────────────────────┘
```

## 设计对齐缺口清单

| 状态 | 设计文档要点                                  | 当前代码表现                  | 后续处理             |
|------|-----------------------------------------------|-------------------------------|----------------------|
| [x]  | 版本号同一记忆下递增                          | calcNextVersionNo 全局取max+1 | 修复为基于旧记忆递增 |
| [x]  | 修订场景需追溯旧记忆                          | 无 parentMemoryId 字段        | 新增字段并填充       |
| [x]  | Executor 应校验 versionStatus                 | 未校验                        | 新增防御性校验       |
| [x]  | 参数校验需校验 type                           | 仅校验 required               | 补充 type 校验       |
| [x]  | Task 的 memoryId/memoryVersionNo 不应冗余更新 | Executor 内重复设置           | 移除冗余更新         |
| [x]  | 前端需有记忆管理页面                          | 页面文件不存在                | 新建完整页面         |
| [x]  | 前端需有版本历史页面                          | 页面文件不存在                | 新建版本历史页面     |

## 执行状态清单

### 六、Java 后端实施步骤

> **执行策略**：此大章节通过逐步骤执行，完成一组立即回写本文档状态。

- [x] 步骤1: AgentMemory Entity 新增 parentMemoryId 字段
- [x] 步骤2: FindAllAgentMemoryRequest DTO 新增 parentMemoryId 查询条件
- [x] 步骤3: PageAgentMemoryResponse DTO 新增 parentMemoryId 字段
- [x] 步骤4: InfoAgentMemoryResponse DTO 新增 parentMemoryId + parentMemoryName 字段
- [x] 步骤5: AgentMemoryCopyMapper 映射新增字段
- [x] 步骤6: AgentMemoryDao.xml insertBatch 新增 parent_memory_id 列
- [x] 步骤7: MPAgentMemoryView 查询条件新增 parentMemoryId 支持
- [x] 步骤8: 新增 MemoryVersionHistoryResponse DTO
- [x] 步骤9: AgentMemoryService 接口新增 findVersionHistory 方法
- [x] 步骤10: DefaultAgentMemoryService 实现 findVersionHistory
- [x] 步骤11: AgentMemoryController 新增版本历史接口
- [x] 步骤12: 修复 DefaultMemoryDistiller.calcNextVersionNo 版本号计算逻辑
- [x] 步骤13: 修复 DefaultMemoryDistiller.createMemoryDraft 填充 parentMemoryId
- [x] 步骤14: 修复 DefaultMemoryExecutor.execute 新增 versionStatus 防御性校验
- [x] 步骤15: 修复 DefaultMemoryExecutor.execute 移除冗余 Task 更新
- [x] 步骤16: 修复 DefaultAgentMemoryService.validateParams 补充 type 校验
- [x] 步骤17: mvn clean package 编译验证

### 七、Web 前端实施步骤

> **执行策略**：此大章节通过逐步骤执行，完成一组立即回写本文档状态。

- [x] 步骤18: 创建 AgentMemoryDto.ts（PageRequest/PageResponse/CreateInfo/UpdateInfo/VersionHistory/ExecuteRequest/ExecuteResponse/ParamsDefinition）
- [x] 步骤19: 创建 agentMemoryApi.ts（page/findOne/create/update/deleteByIds/publish/retire/execute/getParamsDefinition/versionHistory）
- [x] 步骤20: 创建 AgentMemoryManagementPage.tsx（列表页+搜索+新建/编辑弹窗+执行弹窗+发布/退役操作）
- [x] 步骤21: 创建 AgentMemoryVersionPage.tsx（版本历史时间线页）
- [x] 步骤22: 更新 AppRouter.tsx 修正页面导入路径
- [x] 步骤23: 更新 BasicLayoutComponent.tsx 补充菜单项
- [x] 步骤24: npm run build 编译验证

### 八、深度自检

- [x] 步骤25: code-inspector 深度自检（Java）
- [x] 步骤26: web-code-inspector 深度自检（前端）

### 深度自检记录

#### Web 前端自检结果

**一票否决项：**

- ✅ 表头 nowrap：全局CSS已包含
- ✅ 状态列 Tag 颜色：versionStatus 使用 Tag + 颜色映射
- ✅ 操作列更多 Dropdown：编辑外露 + 更多下拉，删除 danger: true
- ✅ 备注列 15字截断 + Tooltip：已修复（原仅 ellipsis）
- ✅ 防重复提交：handleSubmit/handleBatchDelete/handleExecSubmit 均使用 usePreventDoubleClickHook

**关键修复：**

1. 备注列增加 15字截断逻辑（v.length > 15 时截断加"..."）
2. 删除操作改为 Popconfirm 确认弹窗（okButtonProps danger: true）
3. Input 增加 height: 36 样式
4. 版本历史导航改用 React Router navigate（替代 window.location.hash）
5. detailData 类型从 AgentMemoryPageResponseDto 改为 AgentMemoryInfoResponseDto
6. 中文引号冲突修复（AgentMemoryVersionPage.tsx）

## 重要文件索引表

| 文件                            | 路径                                                     | 改动类型 | 说明                                   |
|---------------------------------|----------------------------------------------------------|----------|----------------------------------------|
| AgentMemory                     | common/entity/agentMemory/AgentMemory.java               | 修改     | 新增 parentMemoryId 字段               |
| FindAllAgentMemoryRequest       | common/dto/agentMemory/FindAllAgentMemoryRequest.java    | 修改     | 新增 parentMemoryId 查询条件           |
| PageAgentMemoryResponse         | common/dto/agentMemory/PageAgentMemoryResponse.java      | 修改     | 新增 parentMemoryId 字段               |
| InfoAgentMemoryResponse         | common/dto/agentMemory/InfoAgentMemoryResponse.java      | 修改     | 新增 parentMemoryId + parentMemoryName |
| MemoryVersionHistoryResponse    | common/dto/agentMemory/MemoryVersionHistoryResponse.java | 新增     | 版本历史响应 DTO                       |
| AgentMemoryCopyMapper           | common/copy/agentMemory/AgentMemoryCopyMapper.java       | 修改     | 新增字段映射                           |
| AgentMemoryDao.xml              | resources/mapper/AgentMemoryDao.xml                      | 修改     | insertBatch 新增列                     |
| MPAgentMemoryView               | view/agentMemory/MPAgentMemoryView.java                  | 修改     | 查询条件新增                           |
| AgentMemoryService              | common/service/agentMemory/AgentMemoryService.java       | 修改     | 新增 findVersionHistory                |
| DefaultAgentMemoryService       | service/agentMemory/DefaultAgentMemoryService.java       | 修改     | 实现版本历史+修复参数校验              |
| AgentMemoryController           | controller/agentMemory/AgentMemoryController.java        | 修改     | 新增版本历史接口                       |
| DefaultMemoryDistiller          | service/memory/DefaultMemoryDistiller.java               | 修改     | 修复版本号计算+填充 parentMemoryId     |
| DefaultMemoryExecutor           | service/memory/DefaultMemoryExecutor.java                | 修改     | 新增防御性校验+移除冗余更新            |
| AgentMemoryDto.ts               | web/src/dto/agentMemory/AgentMemoryDto.ts                | 新增     | 前端 DTO                               |
| agentMemoryApi.ts               | web/src/api/agentMemoryApi.ts                            | 新增     | 前端 API                               |
| AgentMemoryManagementPage.tsx   | web/src/pages/AgentMemoryManagementPage.tsx              | 新增     | 记忆管理页面                           |
| AgentMemoryVersionPage.tsx      | web/src/pages/AgentMemoryVersionPage.tsx                 | 新增     | 版本历史页面                           |
| AppRouter.tsx                   | web/src/router/AppRouter.tsx                             | 修改     | 修正导入路径                           |
| BasicLayoutComponent.tsx        | web/src/components/layout/BasicLayoutComponent.tsx       | 修改     | 补充菜单项                             |
| migration_v3_memory_lineage.sql | doc/sql/migration_v3_memory_lineage.sql                  | 新增     | DDL 迁移脚本                           |