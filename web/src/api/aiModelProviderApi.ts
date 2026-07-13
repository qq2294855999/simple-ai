import { http } from "./http";
import type { AiModelProviderResponseDto, AiModelProviderSaveRequestDto } from "../dto/aiModelProvider/AiModelProviderDto";

/**
 * AI 模型供应商 API 封装。
 *
 * @author qty
 */
export const AiModelProviderApi = {
  /** 查询全部供应商 */
  list: () =>
    http.get<AiModelProviderResponseDto[]>("/sys/ai-model-provider/list"),

  /** 保存供应商 */
  save: (data: AiModelProviderSaveRequestDto) =>
    http.post<string>("/sys/ai-model-provider/save", data),

  /** 删除供应商 */
  deleteById: (id: string) =>
    http.delete(`/sys/ai-model-provider/delete/${id}`),
};
