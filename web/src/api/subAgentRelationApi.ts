import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { SubAgentRelationPageRequestDto, SubAgentRelationPageResponseDto, CreateSubAgentRelationRequestDto, UpdateSubAgentRelationRequestDto } from "../dto/subAgentRelation/SubAgentRelationDto";

/**
 * 子智能体关系 API 封装。
 *
 * @author qty
 */
export const SubAgentRelationApi = {
  /** 聚合分页查询 */
  page: (params: SubAgentRelationPageRequestDto) =>
    http.get<PageResult<SubAgentRelationPageResponseDto>>("/sys/sub-agent-relation/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<SubAgentRelationPageResponseDto>(`/sys/sub-agent-relation/find/${id}`),

  /** 创建 */
  create: (data: CreateSubAgentRelationRequestDto) =>
    http.post<string>("/sys/sub-agent-relation/create", data),

  /** 更新 */
  update: (id: string, data: UpdateSubAgentRelationRequestDto) =>
    http.put(`/sys/sub-agent-relation/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/sub-agent-relation/deletes", { data: ids }),
};
