/**
 * 智能体简要信息（用于下拉选择和列表展示）。
 *
 * @author qty
 */
export interface AgentDefinitionMiniDto {
  /** 主键 */
  id: string;
  /** 智能体名称 */
  name: string;
  /** 默认模型主键 */
  defaultModelId?: string;
  /** 状态 */
  status?: string;
  /** 备注 */
  remark?: string;
}
