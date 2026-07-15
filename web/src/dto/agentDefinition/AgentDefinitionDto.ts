/**
 * 智能体定义管理 DTO。
 *
 * @author qty
 */

/** 聚合列表响应（对应 PageAggregateAgentDefinitionResponse） */
export interface AgentDefinitionPageDto {
  /** 主键 */
  id: string;
  /** 名称 */
  name: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 模型 */
  model: string;
  /** 默认模型主键 */
  defaultModelId?: string;
  /** 状态码（1=启用/0=停用） */
  status?: number;
  /** 技能数量 */
  skillCount?: number;
  /** 规则数量 */
  ruleCount?: number;
  /** 记忆数量 */
  memoryCount?: number;
  /** 子智能体数量 */
  subAgentCount?: number;
  /** 最近任务状态 */
  recentTaskStatus?: string;
  /** 最近任务状态说明 */
  recentTaskStatusLabel?: string;
  /** 最近任务时间 */
  latestTaskTime?: string;
  /** 创建时间 */
  createTime?: string;
  /** 修改时间 */
  updateTime?: string;
  /** 备注 */
  remark?: string;
}

/** 明细响应（对应 InfoAgentDefinitionResponse） */
export interface AgentDefinitionInfoDto {
  /** 主键 */
  id: string;
  /** 名称 */
  name: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 第一铁律 */
  firstPrinciple?: string;
  /** 第二规则 */
  secondRule?: string;
  /** 第三技能 */
  thirdSkill?: string;
  /** 模型 */
  model?: string;
  /** 默认模型主键 */
  defaultModelId?: string;
  /** 状态码（1=启用/0=停用） */
  status?: number;
  /** 备注 */
  remark?: string;
}

/** 创建请求（对应 CreateAgentDefinitionRequest） */
export interface CreateAgentDefinitionDto {
  /** 名称 */
  name: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 第一铁律（系统级常量，运行时自动注入） */
  firstPrinciple?: string;
  /** 第二规则（系统级常量，运行时自动注入） */
  secondRule?: string;
  /** 第三技能（系统级常量，运行时自动注入） */
  thirdSkill?: string;
  /** 默认模型主键 */
  defaultModelId?: string;
  /** 备注 */
  remark?: string;
}

/** 更新请求（对应 UpdateAgentDefinitionRequest） */
export interface UpdateAgentDefinitionDto extends CreateAgentDefinitionDto {
  /** 主键 */
  id: string;
}
