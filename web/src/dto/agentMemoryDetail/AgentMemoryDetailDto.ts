/**
 * 智能体记忆详情聚合分页请求参数。
 *
 * @author qty
 */
export interface AgentMemoryDetailPageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配步骤名称或记忆名称） */
  keyword?: string;
  /** 记忆ID精确筛选 */
  agentMemoryId?: string;
  /** 步骤类型筛选 */
  stepType?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 智能体记忆详情聚合分页响应条目。
 *
 * @author qty
 */
export interface AgentMemoryDetailPageResponseDto {
  /** 主键 */
  id: string;
  /** 记忆ID */
  agentMemoryId: string;
  /** 记忆名称 */
  memoryName: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
  /** 步骤名称 */
  stepName: string;
  /** 步骤类型 */
  stepType: string;
  /** 步骤类型标签 */
  stepTypeLabel: string;
  /** 执行内容 */
  execContent: string;
  /** 返回数据格式 */
  returnDataFormat: string;
  /** 父步骤ID */
  parentStepId: string;
  /** 父步骤名称 */
  parentStepName: string;
  /** 下一步骤ID */
  nextStepId: string;
  /** 下一步骤名称 */
  nextStepName: string;
  /** 分支条件 */
  branchCondition: string;
  /** 分支路由 */
  branchRoute: string;
  /** 模型 */
  model: string;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
}

/**
 * 智能体记忆详情创建请求。
 *
 * @author qty
 */
export interface CreateAgentMemoryDetailRequestDto {
  /** 记忆ID */
  agentMemoryId: string;
  /** 步骤名称 */
  stepName: string;
  /** 步骤类型 */
  stepType: string;
  /** 执行内容 */
  execContent: string;
  /** 返回数据格式 */
  returnDataFormat: string;
  /** 父步骤ID */
  parentStepId?: string;
  /** 下一步骤ID */
  nextStepId?: string;
  /** 分支条件 */
  branchCondition?: string;
  /** 分支路由 */
  branchRoute?: string;
  /** 模型 */
  model?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 智能体记忆详情更新请求。
 *
 * @author qty
 */
export interface UpdateAgentMemoryDetailRequestDto extends CreateAgentMemoryDetailRequestDto {
  /** 主键 */
  id: string;
}
