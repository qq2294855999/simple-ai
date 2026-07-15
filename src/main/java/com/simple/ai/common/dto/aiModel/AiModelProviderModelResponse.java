package com.simple.ai.common.dto.aiModel;

import lombok.Data;

/**
 * 供应商远程模型列表项响应。
 * 用于从供应商 OpenAI 兼容接口拉取可用模型列表后返回前端供选择。
 *
 * @author qty
 */
@Data
public class AiModelProviderModelResponse {

    /** 模型编码 */
    private String modelCode;

    /** 模型名称 */
    private String modelName;
}
