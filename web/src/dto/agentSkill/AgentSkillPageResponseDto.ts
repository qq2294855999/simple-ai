/**
 * 智能体技能聚合分页响应条目。
 *
 * @author qty
 */
export interface AgentSkillPageResponseDto {
  /** 主键 */
  id: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
  /** 定义描述 */
  definitionDesc: string;
  /** 执行内容 */
  execContent: string;
  /** 返回的数据格式 */
  returnDataFormat: string;
  /** 命令数量 */
  commandCount: number;
  /** 修改时间 */
  updateTime: string;
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
}
