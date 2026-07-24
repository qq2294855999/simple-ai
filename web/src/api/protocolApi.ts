import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    CreateProtocolRequestDto,
    ProtocolInfoResponseDto,
    ProtocolPageRequestDto,
    ProtocolPageResponseDto,
    UpdateProtocolRequestDto
} from "../dto/protocol/ProtocolDto";

/**
 * 执行器协议 API 封装。
 *
 * @author qty
 */
export const ProtocolApi = {
    /** 分页查询 */
    page: (params: ProtocolPageRequestDto) =>
        http.get<PageResult<ProtocolPageResponseDto>>("/agent/protocol/page", {params}),

    /** 查询单个 */
    findOne: (id: string) =>
        http.get<ProtocolInfoResponseDto>(`/agent/protocol/${id}`),

    /** 创建 */
    create: (data: CreateProtocolRequestDto) =>
        http.post<string>("/agent/protocol", data),

    /** 更新 */
    update: (id: string, data: UpdateProtocolRequestDto) =>
        http.put(`/agent/protocol`, data),

    /** 删除单个 */
    deleteById: (id: string) =>
        http.delete(`/agent/protocol/${id}`),

    /** 切换启用/停用状态 */
    toggleStatus: (id: string) =>
        http.put<string>(`/agent/protocol/${id}/toggle-status`),

    /** 批量删除 */
    deleteByIds: (ids: string[]) =>
        Promise.all(ids.map(id => http.delete(`/agent/protocol/${id}`))).then(() => undefined)
};