import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { AgentMemoryDetailPageRequestDto, AgentMemoryDetailPageResponseDto, CreateAgentMemoryDetailRequestDto, UpdateAgentMemoryDetailRequestDto } from "../dto/agentMemoryDetail/AgentMemoryDetailDto";

/**
 * 智能体记忆详情 API 封装。
 *
 * @author qty
 */
export const AgentMemoryDetailApi = {
  /** 聚合分页查询 */
  page: (params: AgentMemoryDetailPageRequestDto) =>
    http.get<PageResult<AgentMemoryDetailPageResponseDto>>("/sys/agent-memory-detail/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<AgentMemoryDetailPageResponseDto>(`/sys/agent-memory-detail/find/${id}`),

  /** 创建 */
  create: (data: CreateAgentMemoryDetailRequestDto) =>
    http.post<string>("/sys/agent-memory-detail/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentMemoryDetailRequestDto) =>
    http.put(`/sys/agent-memory-detail/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-memory-detail/deletes", { data: ids }),
};
