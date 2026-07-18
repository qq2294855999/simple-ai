package com.simple.ai.common.view.aiUser;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.entity.aiUser.AiUser;

import java.util.List;

/**
 * AI 平台用户视图接口。
 *
 * @author qty
 */
public interface AiUserView {

    /**
     * 分页查询用户列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAiUserResponse> findAll(PageAiUserRequest pageRequest, Page<PageAiUserResponse> page);

    /**
     * 按主键查询用户。
     *
     * @param id 用户主键
     * @return 用户实体，不存在返回 null
     */
    AiUser findById(String id);

    /**
     * 保存用户。
     *
     * @param entity 用户实体
     */
    void save(AiUser entity);

    /**
     * 按主键更新用户。
     *
     * @param entity 用户实体
     */
    void updateById(AiUser entity);

    /**
     * 按主键删除用户。
     *
     * @param id 用户主键
     */
    void deleteById(String id);
}
