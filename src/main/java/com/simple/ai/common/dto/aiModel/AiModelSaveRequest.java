package com.simple.ai.common.dto.aiModel;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 模型保存请求。
 *
 * @author qty
 */
@Data
public class AiModelSaveRequest {

    /** 模型主键，空表示创建 */
    private String id;

    /** 供应商主键 */
    @NotBlank(message = "供应商不能为空")
    private String providerId;

    /** 模型编码 */
    @NotBlank(message = "模型编码不能为空")
    private String modelCode;

    /** 模型名称 */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /** 能力配置 */
    private String capabilityConfig;

    /** 上下文窗口 */
    private Integer contextWindow;

    /** 是否供应商默认模型 */
    private Boolean providerDefault;

    /** 是否系统默认模型 */
    private Boolean systemDefault;

    /** 启停状态 */
    private Integer status;

    /** 备注 */
    private String remark;
}
