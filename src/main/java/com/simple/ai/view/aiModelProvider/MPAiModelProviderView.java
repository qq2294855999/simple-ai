package com.simple.ai.view.aiModelProvider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import com.simple.ai.common.view.aiModelProvider.AiModelProviderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 模型供应商视图实现。
 *
 * @author qty
 */
@Component
class MPAiModelProviderView implements AiModelProviderView {

    @Autowired
    private AiModelProviderRepository repository;

    @Override
    public AiModelProvider findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public AiModelProvider findByProviderCode(String providerCode) {
        LambdaQueryWrapper<AiModelProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProvider::getProviderCode, providerCode);
        return repository.selectOne(wrapper);
    }

    @Override
    public List<AiModelProvider> findAll() {
        LambdaQueryWrapper<AiModelProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AiModelProvider::getProviderName);
        return repository.selectList(wrapper);
    }

    @Override
    public void save(AiModelProvider provider) {
        repository.insert(provider);
    }

    @Override
    public void updateById(AiModelProvider provider) {
        repository.updateById(provider);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
