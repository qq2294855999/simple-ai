package com.simple.ai.view.aiUser;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.entity.aiUser.AiUser;
import com.simple.ai.common.view.aiUser.AiUserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 平台用户视图实现。
 *
 * @author qty
 */
@Component
class MPAiUserView implements AiUserView {

    @Autowired
    private AiUserRepository repository;

    @Override
    public List<PageAiUserResponse> findAll(PageAiUserRequest pageRequest, Page<PageAiUserResponse> page) {
        return repository.selectPage(pageRequest, page);
    }

    @Override
    public AiUser findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AiUser entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(AiUser entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
