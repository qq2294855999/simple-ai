/**
 * AI 模型供应商管理 DTO。
 *
 * @author qty
 */

/** 供应商保存请求 */
export interface AiModelProviderSaveRequestDto {
  /** 供应商主键，空表示创建 */
  id?: string;
  /** 供应商编码 */
  providerCode: string;
  /** 供应商名称 */
  providerName: string;
  /** 协议类型 */
  protocolType: string;
  /** 服务根地址 */
  baseUrl: string;
  /** API Key，仅创建必填 */
  apiKey?: string;
  /** 超时毫秒数 */
  timeoutMillis: number;
  /** 是否系统默认供应商 */
  systemDefault?: boolean;
  /** 启停状态 */
  status?: number;
  /** 备注 */
  remark?: string;
}

/** 供应商响应 */
export interface AiModelProviderResponseDto {
  /** 供应商主键 */
  id: string;
  /** 供应商编码 */
  providerCode: string;
  /** 供应商名称 */
  providerName: string;
  /** 协议类型 */
  protocolType: string;
  /** 服务根地址 */
  baseUrl: string;
  /** 是否已经配置 API Key */
  apiKeyConfigured: boolean;
  /** 超时毫秒数 */
  timeoutMillis: number;
  /** 是否系统默认供应商 */
  systemDefault: boolean;
  /** 启停状态 */
  status: number;
  /** 备注 */
  remark: string;
  /** 最后修改时间 */
  updateTime: string;
}
