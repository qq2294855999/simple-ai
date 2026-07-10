/**
 * 智能体技能创建请求。
 *
 * @author qty
 */
export interface CreateAgentSkillRequestDto {
  /** 智能体ID */
  agentId: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 执行内容 */
  execContent: string;
  /** 返回的数据格式 */
  returnDataFormat: string;
  /** 备注 */
  remark?: string;
}

/**
 * 智能体技能更新请求。
 *
 * @author qty
 */
export interface UpdateAgentSkillRequestDto extends CreateAgentSkillRequestDto {
  /** 主键 */
  id: string;
}
