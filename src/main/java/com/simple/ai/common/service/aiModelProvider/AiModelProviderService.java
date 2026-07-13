package com.simple.ai.common.service.aiModelProvider;

import com.simple.ai.common.dto.aiModelProvider.AiModelProviderResponse;
import com.simple.ai.common.dto.aiModelProvider.AiModelProviderSaveRequest;

import java.util.List;

/**
 * AI 模型供应商管理服务。
 *
 * @author qty
 */
public interface AiModelProviderService {

    /**
     * 查询全部供应商。
     *
     * @return 安全供应商列表
     */
    List<AiModelProviderResponse> findAll();

    /**
     * 保存供应商。
     *
     * @param request 保存请求
     * @return 供应商主键
     */
    String save(AiModelProviderSaveRequest request);

    /**
     * 删除供应商。
     *
     * @param id 供应商主键
     */
    void deleteById(String id);
}
