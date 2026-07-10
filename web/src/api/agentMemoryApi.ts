import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { AgentMemoryPageRequestDto, AgentMemoryPageResponseDto, CreateAgentMemoryRequestDto, UpdateAgentMemoryRequestDto } from "../dto/agentMemory/AgentMemoryDto";

/**
 * 智能体记忆 API 封装。
 *
 * @author qty
 */
export const AgentMemoryApi = {
  /** 聚合分页查询 */
  page: (params: AgentMemoryPageRequestDto) =>
    http.get<PageResult<AgentMemoryPageResponseDto>>("/sys/agent-memory/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<AgentMemoryPageResponseDto>(`/sys/agent-memory/find/${id}`),

  /** 创建 */
  create: (data: CreateAgentMemoryRequestDto) =>
    http.post<string>("/sys/agent-memory/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentMemoryRequestDto) =>
    http.put(`/sys/agent-memory/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-memory/deletes", { data: ids }),

  /** 全量列表（用于下拉选择） */
  listAll: () =>
    http.get<PageResult<AgentMemoryPageResponseDto>>("/sys/agent-memory/aggregate-list", { params: { current: 1, size: 1000 } }),
};
