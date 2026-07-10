import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { AgentDefinitionMiniDto } from "../dto/agentDefinition/AgentDefinitionMiniDto";

/**
 * 智能体定义 API 封装（用于下拉选择等辅助场景）。
 *
 * @author qty
 */
export const AgentDefinitionApi = {
  /** 全量列表（用于下拉选择） */
  listAll: () =>
    http.get<PageResult<AgentDefinitionMiniDto>>("/sys/agent-definition/aggregate-list", {
      params: { current: 1, size: 1000 }
    }),
};
