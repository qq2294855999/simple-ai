package com.simple.ai.common.dto.aiModelProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 模型供应商保存请求。
 *
 * @author qty
 */
@Data
@Schema(title = "AI模型供应商保存请求")
public class AiModelProviderSaveRequest {

    @Schema(description = "供应商主键，空表示创建")
    private String id;

    @Schema(description = "供应商编码")
    @NotEmpty(message = "供应商编码不能为空")
    private String providerCode;

    @Schema(description = "供应商名称")
    @NotEmpty(message = "供应商名称不能为空")
    private String providerName;

    @Schema(description = "协议类型")
    @NotEmpty(message = "协议类型不能为空")
    private String protocolType;

    @Schema(description = "OpenAI 兼容服务根地址")
    @NotEmpty(message = "服务地址不能为空")
    private String baseUrl;

    @Schema(description = "API Key，仅创建必填；编辑时非空才更新")
    private String apiKey;

    @Schema(description = "超时毫秒数")
    @NotNull(message = "超时不能为空")
    private Integer timeoutMillis;

    @Schema(description = "是否系统默认供应商")
    private Boolean systemDefault;

    @Schema(description = "启停状态")
    @NotNull(message = "启停状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}