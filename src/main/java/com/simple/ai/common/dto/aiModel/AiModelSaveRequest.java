package com.simple.ai.common.dto.aiModel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 模型保存请求。
 *
 * @author qty
 */
@Data
@Schema(title = "AI模型保存请求")
public class AiModelSaveRequest {

    @Schema(description = "模型主键，空表示创建")
    private String id;

    @Schema(description = "供应商主键")
    @NotEmpty(message = "供应商不能为空")
    private String providerId;

    @Schema(description = "模型编码")
    @NotEmpty(message = "模型编码不能为空")
    private String modelCode;

    @Schema(description = "模型名称")
    @NotEmpty(message = "模型名称不能为空")
    private String modelName;

    @Schema(description = "能力配置")
    private String capabilityConfig;

    @Schema(description = "上下文窗口")
    private Integer contextWindow;

    @Schema(description = "是否供应商默认模型")
    private Boolean providerDefault;

    @Schema(description = "是否系统默认模型")
    private Boolean systemDefault;

    @Schema(description = "启停状态")
    @NotNull(message = "启停状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}