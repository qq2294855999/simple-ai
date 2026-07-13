package com.simple.ai.common.dto.aiModelProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    /** 供应商主键，空表示创建 */
    private String id;

    /** 供应商编码 */
    @NotBlank(message = "供应商编码不能为空")
    private String providerCode;

    /** 供应商名称 */
    @NotBlank(message = "供应商名称不能为空")
    private String providerName;

    /** 协议类型 */
    @NotBlank(message = "协议类型不能为空")
    private String protocolType;

    /** OpenAI 兼容服务根地址 */
    @NotBlank(message = "服务地址不能为空")
    private String baseUrl;

    /** API Key，仅创建必填；编辑时非空才更新 */
    private String apiKey;

    /** 超时毫秒数 */
    @NotNull(message = "超时不能为空")
    private Integer timeoutMillis;

    /** 是否系统默认供应商 */
    private Boolean systemDefault;

    /** 启停状态 */
    private Integer status;

    /** 备注 */
    private String remark;
}
