package com.simple.ai.service.aiModel;

import com.simple.ai.common.dto.aiModel.AiModelRuntimeConfig;

/**
 * AI 模型运行时路由服务。
 *
 * @author qty
 */
public interface AiModelRoutingService {

    /**
     * 解析当前调用可用的模型配置。
     *
     * @param explicitModelId 请求显式模型主键
     * @param agentId 智能体主键
     * @return 运行时模型配置
     */
    AiModelRuntimeConfig resolve(String explicitModelId, String agentId);
}
