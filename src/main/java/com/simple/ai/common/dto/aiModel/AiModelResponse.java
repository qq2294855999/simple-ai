package com.simple.ai.common.dto.aiModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 模型管理与选择响应。
 *
 * @author qty
 */
@Data
@Schema(title = "AI模型响应")
public class AiModelResponse {

    @Schema(description = "模型主键")
    private String id;

    @Schema(description = "供应商主键")
    private String providerId;

    @Schema(description = "供应商名称")
    private String providerName;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "模型编码")
    private String modelCode;

    @Schema(description = "模型名称")
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
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "最后修改时间")
    private Date updateTime;
}