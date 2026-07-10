/**
 * 智能体规则聚合分页请求参数。
 *
 * @author qty
 */
export interface AgentRulePageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配智能体名称或描述） */
  keyword?: string;
  /** 智能体ID精确筛选 */
  agentId?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 智能体规则聚合分页响应条目。
 *
 * @author qty
 */
export interface AgentRulePageResponseDto {
  /** 主键 */
  id: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 触发条件 */
  triggerCondition: string;
  /** 触发动作 */
  triggerAction: string;
  /** 修改时间 */
  updateTime: string;
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
}

/**
 * 智能体规则创建请求。
 *
 * @author qty
 */
export interface CreateAgentRuleRequestDto {
  /** 智能体ID */
  agentId: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 触发条件 */
  triggerCondition: string;
  /** 触发动作 */
  triggerAction: string;
  /** 备注 */
  remark?: string;
}

/**
 * 智能体规则更新请求。
 *
 * @author qty
 */
export interface UpdateAgentRuleRequestDto extends CreateAgentRuleRequestDto {
  /** 主键 */
  id: string;
}
