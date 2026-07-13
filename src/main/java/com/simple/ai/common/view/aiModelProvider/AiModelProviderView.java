package com.simple.ai.common.view.aiModelProvider;

import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;

import java.util.List;

/**
 * AI 模型供应商视图。
 *
 * @author qty
 */
public interface AiModelProviderView {

    /**
     * 按主键查询供应商。
     *
     * @param id 供应商主键
     * @return 供应商配置
     */
    AiModelProvider findById(String id);

    /**
     * 按编码查询供应商。
     *
     * @param providerCode 供应商编码
     * @return 供应商配置
     */
    AiModelProvider findByProviderCode(String providerCode);

    /**
     * 查询全部供应商。
     *
     * @return 供应商列表
     */
    List<AiModelProvider> findAll();

    /**
     * 保存供应商。
     *
     * @param provider 供应商配置
     */
    void save(AiModelProvider provider);

    /**
     * 按主键更新供应商。
     *
     * @param provider 供应商配置
     */
    void updateById(AiModelProvider provider);

    /**
     * 按主键删除供应商。
     *
     * @param id 供应商主键
     */
    void deleteById(String id);
}
