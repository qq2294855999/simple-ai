package com.simple.ai.common.dto.aiModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商远程模型列表项响应。
 * 用于从供应商 OpenAI 兼容接口拉取可用模型列表后返回前端供选择。
 *
 * @author qty
 */
@Data
@Schema(title = "供应商远程模型列表项响应")
public class AiModelProviderModelResponse {

    @Schema(description = "模型编码")
    private String modelCode;

    @Schema(description = "模型名称")
    private String modelName;
}