import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { TaskPageRequestDto, TaskPageResponseDto, CreateTaskRequestDto, UpdateTaskRequestDto } from "../dto/task/TaskDto";

/**
 * 任务 API 封装。
 *
 * @author qty
 */
export const TaskApi = {
  /** 聚合分页查询 */
  page: (params: TaskPageRequestDto) =>
    http.get<PageResult<TaskPageResponseDto>>("/sys/task/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<TaskPageResponseDto>(`/sys/task/find/${id}`),

  /** 创建 */
  create: (data: CreateTaskRequestDto) =>
    http.post<string>("/sys/task/create", data),

  /** 更新 */
  update: (id: string, data: UpdateTaskRequestDto) =>
    http.put(`/sys/task/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/task/deletes", { data: ids }),
};
