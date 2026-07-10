import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { AtomicCommandPageRequestDto, AtomicCommandPageResponseDto, CreateAtomicCommandRequestDto, UpdateAtomicCommandRequestDto } from "../dto/atomicCommand/AtomicCommandDto";

/**
 * 原子命令 API 封装。
 *
 * @author qty
 */
export const AtomicCommandApi = {
  /** 聚合分页查询 */
  page: (params: AtomicCommandPageRequestDto) =>
    http.get<PageResult<AtomicCommandPageResponseDto>>("/sys/atomic-command/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<AtomicCommandPageResponseDto>(`/sys/atomic-command/find/${id}`),

  /** 创建 */
  create: (data: CreateAtomicCommandRequestDto) =>
    http.post<string>("/sys/atomic-command/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAtomicCommandRequestDto) =>
    http.put(`/sys/atomic-command/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/atomic-command/deletes", { data: ids }),
};
