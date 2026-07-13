import { http } from "./http";
import type { AiModelResponseDto, AiModelSaveRequestDto } from "../dto/aiModel/AiModelDto";

/**
 * AI 模型管理 API 封装。
 *
 * @author qty
 */
export const AiModelApi = {
  /** 查询全部模型 */
  list: () =>
    http.get<AiModelResponseDto[]>("/sys/ai-model/list"),

  /** 查询智能体可用模型 */
  available: (agentId: string) =>
    http.get<AiModelResponseDto[]>(`/sys/ai-model/available/${agentId}`),

  /** 保存模型 */
  save: (data: AiModelSaveRequestDto) =>
    http.post<string>("/sys/ai-model/save", data),

  /** 删除模型 */
  deleteById: (id: string) =>
    http.delete(`/sys/ai-model/delete/${id}`),
};
