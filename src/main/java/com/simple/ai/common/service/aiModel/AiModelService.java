package com.simple.ai.common.service.aiModel;

import com.simple.ai.common.dto.aiModel.AiModelProviderModelResponse;
import com.simple.ai.common.dto.aiModel.AiModelResponse;
import com.simple.ai.common.dto.aiModel.AiModelSaveRequest;

import java.util.List;

/**
 * AI 模型管理服务。
 *
 * @author qty
 */
public interface AiModelService {

    /**
     * 查询全部模型。
     *
     * @return 模型列表
     */
    List<AiModelResponse> findAll();

    /**
     * 查询指定智能体可选择的模型。
     *
     * @param agentId 智能体主键
     * @return 启用模型列表
     */
    List<AiModelResponse> findAvailableByAgentId(String agentId);

    /**
     * 保存模型。
     *
     * @param request 保存请求
     * @return 模型主键
     */
    String save(AiModelSaveRequest request);

    /**
     * 从供应商远程拉取可用模型列表。
     *
     * @param providerId 供应商主键
     * @return 远程可用模型列表
     */
    List<AiModelProviderModelResponse> fetchProviderModels(String providerId);

    /**
     * 删除模型。
     *
     * @param id 模型主键
     */
    void deleteById(String id);
}
