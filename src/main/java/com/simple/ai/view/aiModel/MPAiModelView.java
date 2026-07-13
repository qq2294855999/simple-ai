package com.simple.ai.view.aiModel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.entity.aiModel.AiModel;
import com.simple.ai.common.view.aiModel.AiModelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 模型视图实现。
 *
 * @author qty
 */
@Component
class MPAiModelView implements AiModelView {

    @Autowired
    private AiModelRepository repository;

    @Override
    public AiModel findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public AiModel findByProviderIdAndModelCode(String providerId, String modelCode) {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getProviderId, providerId);
        wrapper.eq(AiModel::getModelCode, modelCode);
        return repository.selectOne(wrapper);
    }

    @Override
    public AiModel findSystemDefault() {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getSystemDefault, 1);
        return repository.selectOne(wrapper);
    }

    @Override
    public List<AiModel> findAll() {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AiModel::getModelName);
        return repository.selectList(wrapper);
    }

    @Override
    public List<AiModel> findAllByProviderId(String providerId) {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getProviderId, providerId);
        wrapper.orderByAsc(AiModel::getModelName);
        return repository.selectList(wrapper);
    }

    @Override
    public void save(AiModel model) {
        repository.insert(model);
    }

    @Override
    public void updateById(AiModel model) {
        repository.updateById(model);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
