package com.simple.ai.common.dto.aiModelProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 模型供应商安全响应。
 *
 * @author qty
 */
@Data
@Schema(title = "AI模型供应商响应")
public class AiModelProviderResponse {

    @Schema(description = "供应商主键")
    private String id;

    @Schema(description = "供应商编码")
    private String providerCode;

    @Schema(description = "供应商名称")
    private String providerName;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "服务根地址")
    private String baseUrl;

    @Schema(description = "是否已经配置API Key")
    private Boolean apiKeyConfigured;

    @Schema(description = "超时毫秒数")
    private Integer timeoutMillis;

    @Schema(description = "是否系统默认供应商")
    private Boolean systemDefault;

    @Schema(description = "启停状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "最后修改时间")
    private Date updateTime;
}