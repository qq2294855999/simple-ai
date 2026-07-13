package com.simple.ai.common.view.aiModel;

import com.simple.ai.common.entity.aiModel.AiModel;

import java.util.List;

/**
 * AI 模型视图。
 *
 * @author qty
 */
public interface AiModelView {

    /**
     * 按主键查询模型。
     *
     * @param id 模型主键
     * @return 模型配置
     */
    AiModel findById(String id);

    /**
     * 按供应商和模型编码查询模型。
     *
     * @param providerId 供应商主键
     * @param modelCode 模型编码
     * @return 模型配置
     */
    AiModel findByProviderIdAndModelCode(String providerId, String modelCode);

    /**
     * 查询系统默认模型。
     *
     * @return 系统默认模型
     */
    AiModel findSystemDefault();

    /**
     * 查询全部模型。
     *
     * @return 模型列表
     */
    List<AiModel> findAll();

    /**
     * 按供应商查询模型。
     *
     * @param providerId 供应商主键
     * @return 模型列表
     */
    List<AiModel> findAllByProviderId(String providerId);

    /**
     * 保存模型。
     *
     * @param model 模型配置
     */
    void save(AiModel model);

    /**
     * 按主键更新模型。
     *
     * @param model 模型配置
     */
    void updateById(AiModel model);

    /**
     * 按主键删除模型。
     *
     * @param id 模型主键
     */
    void deleteById(String id);
}
