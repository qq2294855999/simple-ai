package com.simple.ai.service.aiModel;

import com.simple.ai.common.dto.aiModel.AiModelRuntimeConfig;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.aiModel.AiModel;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import com.simple.ai.common.enums.AiModelProviderProtocolProcess;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.aiModel.AiModelView;
import com.simple.ai.common.view.aiModelProvider.AiModelProviderView;
import com.simple.ai.service.aiModelProvider.AiModelProviderApiKeyCipher;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 模型运行时路由服务默认实现。
 *
 * @author qty
 */
@Service
class DefaultAiModelRoutingService implements AiModelRoutingService {

    @Autowired
    private AiModelView aiModelView;

    @Autowired
    private AiModelProviderView aiModelProviderView;

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AiModelProviderApiKeyCipher apiKeyCipher;

    @Override
    public AiModelRuntimeConfig resolve(String explicitModelId, String agentId) {

        // 按固定优先级解析实际模型
        AiModel model = resolveModel(explicitModelId, agentId);

        // 校验模型与供应商启用状态并组装运行时配置
        AiModelProvider provider = loadAvailableProvider(model);
        return buildRuntimeConfig(model, provider);
    }

    /**
     * 按请求、智能体和系统默认优先级解析模型。
     *
     * @param explicitModelId 显式模型主键
     * @param agentId 智能体主键
     * @return 已选模型
     */
    private AiModel resolveModel(String explicitModelId, String agentId) {
        if (explicitModelId != null && !explicitModelId.isBlank()) {
            return loadAvailableModel(explicitModelId);
        }
        AiModel agentModel = findAgentDefaultModel(agentId);
        if (agentModel != null) {
            return loadAvailableModel(agentModel.getId());
        }
        AiModel systemModel = aiModelView.findSystemDefault();
        AssertUtils.notEmpty(systemModel, "未配置可用AI模型");
        return loadAvailableModel(systemModel.getId());
    }

    /**
     * 查询智能体默认模型。
     *
     * @param agentId 智能体主键
     * @return 智能体默认模型
     */
    private AiModel findAgentDefaultModel(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            return null;
        }
        AgentDefinition agent = agentDefinitionView.findById(agentId);
        AssertUtils.notEmpty(agent, "智能体[{}]不存在", agentId);
        String defaultModelId = agent.getDefaultModelId();
        if (defaultModelId == null || defaultModelId.isBlank()) {
            return null;
        }
        return loadAvailableModel(defaultModelId);
    }

    /**
     * 加载启用模型。
     *
     * @param modelId 模型主键
     * @return 启用模型
     */
    private AiModel loadAvailableModel(String modelId) {
        AiModel model = aiModelView.findById(modelId);
        AssertUtils.notEmpty(model, "模型[{}]不存在", modelId);
        AssertUtils.isTrue(Status.ON.equals(model.getStatus()), "模型[{}]未启用", model.getModelName());
        return model;
    }

    /**
     * 加载与模型关联的可用供应商。
     *
     * @param model 模型配置
     * @return 可用供应商
     */
    private AiModelProvider loadAvailableProvider(AiModel model) {
        AiModelProvider provider = aiModelProviderView.findById(model.getProviderId());
        AssertUtils.notEmpty(provider, "模型[{}]关联供应商不存在", model.getModelName());
        AssertUtils.isTrue(Status.ON.equals(provider.getStatus()), "供应商[{}]未启用", provider.getProviderName());
        AssertUtils.isTrue(AiModelProviderProtocolProcess.OPENAI_COMPATIBLE.name().equals(provider.getProtocolType()), "供应商协议不支持");
        return provider;
    }

    /**
     * 构建仅供当前调用使用的运行时配置。
     *
     * @param model 模型配置
     * @param provider 供应商配置
     * @return 运行时配置
     */
    private AiModelRuntimeConfig buildRuntimeConfig(AiModel model, AiModelProvider provider) {
        AiModelRuntimeConfig config = new AiModelRuntimeConfig();
        config.setProviderId(provider.getId());
        config.setProviderName(provider.getProviderName());
        config.setModelId(model.getId());
        config.setModelCode(model.getModelCode());
        config.setBaseUrl(provider.getBaseUrl());
        config.setTimeoutMillis(provider.getTimeoutMillis());
        config.setApiKey(apiKeyCipher.decrypt(provider.getApiKeyCiphertext()));
        return config;
    }
}
