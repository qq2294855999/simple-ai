package com.simple.ai.common.copy.aiUser;

import com.simple.ai.common.dto.aiUser.CreateAiUserRequest;
import com.simple.ai.common.dto.aiUser.InfoAiUserResponse;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.dto.aiUser.UpdateAiUserRequest;
import com.simple.ai.common.entity.aiUser.AiUser;
import org.mapstruct.Mapper;

/**
 * AI 平台用户对象属性复制。
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AiUserCopyMapper {

    /**
     * 将实体转换为分页响应对象。
     *
     * @param entity 用户实体
     * @return 分页响应对象
     */
    PageAiUserResponse toPageResponse(AiUser entity);

    /**
     * 将实体转换为详情响应对象。
     *
     * @param entity 用户实体
     * @return 详情响应对象
     */
    InfoAiUserResponse toInfoResponse(AiUser entity);

    /**
     * 将创建请求转换为实体。
     *
     * @param request 创建请求
     * @return 用户实体
     */
    AiUser toEntity(CreateAiUserRequest request);

    /**
     * 将更新请求转换为实体。
     *
     * @param request 更新请求
     * @return 用户实体
     */
    AiUser toEntity(UpdateAiUserRequest request);
}
