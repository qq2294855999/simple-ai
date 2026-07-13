package com.simple.ai.common.dto.aiModelProvider;

import lombok.Data;

import java.util.Date;

/**
 * AI 模型供应商安全响应。
 *
 * @author qty
 */
@Data
public class AiModelProviderResponse {

    /** 供应商主键 */
    private String id;

    /** 供应商编码 */
    private String providerCode;

    /** 供应商名称 */
    private String providerName;

    /** 协议类型 */
    private String protocolType;

    /** 服务根地址 */
    private String baseUrl;

    /** 是否已经配置 API Key */
    private Boolean apiKeyConfigured;

    /** 超时毫秒数 */
    private Integer timeoutMillis;

    /** 是否系统默认供应商 */
    private Boolean systemDefault;

    /** 启停状态 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 最后修改时间 */
    private Date updateTime;
}
