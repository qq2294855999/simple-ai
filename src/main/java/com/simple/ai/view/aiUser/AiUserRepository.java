package com.simple.ai.view.aiUser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.entity.aiUser.AiUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 平台用户数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AiUserRepository extends BaseMapper<AiUser> {

    /**
     * 分页查询用户列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAiUserResponse> selectPage(@Param("pageRequest") PageAiUserRequest pageRequest, Page<PageAiUserResponse> page);
}
