/**
 * AI 模型管理 DTO。
 *
 * @author qty
 */

/** 模型保存请求 */
export interface AiModelSaveRequestDto {
  /** 模型主键，空表示创建 */
  id?: string;
  /** 供应商主键 */
  providerId: string;
  /** 模型编码 */
  modelCode: string;
  /** 模型名称 */
  modelName: string;
  /** 能力配置 */
  capabilityConfig?: string;
  /** 上下文窗口 */
  contextWindow?: number;
  /** 是否供应商默认模型 */
  providerDefault?: boolean;
  /** 是否系统默认模型 */
  systemDefault?: boolean;
  /** 启停状态 */
  status?: number;
  /** 备注 */
  remark?: string;
}

/** 模型响应 */
export interface AiModelResponseDto {
  /** 模型主键 */
  id: string;
  /** 供应商主键 */
  providerId: string;
  /** 供应商名称 */
  providerName: string;
  /** 协议类型 */
  protocolType: string;
  /** 模型编码 */
  modelCode: string;
  /** 模型名称 */
  modelName: string;
  /** 能力配置 */
  capabilityConfig: string;
  /** 上下文窗口 */
  contextWindow: number;
  /** 是否供应商默认模型 */
  providerDefault: boolean;
  /** 是否系统默认模型 */
  systemDefault: boolean;
  /** 启停状态 */
  status: number;
  /** 备注 */
  remark: string;
  /** 最后修改时间 */
  updateTime: string;
}

/** 供应商远程模型列表项 */
export interface AiModelProviderModelDto {
  /** 模型编码 */
  modelCode: string;
  /** 模型名称 */
  modelName: string;
}
