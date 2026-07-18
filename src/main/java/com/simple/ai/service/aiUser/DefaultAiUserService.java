package com.simple.ai.service.aiUser;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.aiUser.AiUserCopyMapper;
import com.simple.ai.common.dto.aiUser.CreateAiUserRequest;
import com.simple.ai.common.dto.aiUser.InfoAiUserResponse;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.dto.aiUser.UpdateAiUserRequest;
import com.simple.ai.common.entity.aiUser.AiUser;
import com.simple.ai.common.service.aiUser.AiUserService;
import com.simple.ai.common.view.aiUser.AiUserView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.oauth.start.common.dto.user.CreateUserRequest;
import com.simple.common.oauth.start.common.dto.user.UpdateUserRequest;
import com.simple.common.oauth.start.common.dto.user.UserInfoResponse;
import com.simple.common.oauth.start.common.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * AI 平台用户服务默认实现。
 * <p>
 * 核心职责：管理 AI 业务域用户，创建时先调 OAuth 注册基础账号。
 * </p>
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAiUserService implements AiUserService {

    @Autowired
    private AiUserView aiUserView;

    @Autowired
    private AiUserCopyMapper copy;

    @Autowired
    private UserService userService;

    @Override
    public IPage<PageAiUserResponse> findAll(PageAiUserRequest pageRequest) {

        // 构建分页对象
        Page<PageAiUserResponse> page = pageRequest.getPage(PageAiUserResponse.class);

        // 执行分页查询
        List<PageAiUserResponse> records = aiUserView.findAll(pageRequest, page);
        page.setRecords(records);

        return page;
    }

    @Override
    public InfoAiUserResponse findById(String id) {

        // 查询本地用户记录
        AiUser entity = aiUserView.findById(id);
        AssertUtils.notEmpty(entity, "用户[{}]不存在", id);

        // 基础字段映射
        InfoAiUserResponse response = copy.toInfoResponse(entity);

        // 从授权中心补充基础信息（username、phone 等）
        augmentFromOauth(response, id);

        return response;
    }

    @Override
    public String create(CreateAiUserRequest request) {

        // 参数校验：昵称不能为空
        AssertUtils.notEmpty(request.getNickname(), "昵称不能为空");

        // 参数校验：用户账号不能为空
        AssertUtils.notEmpty(request.getUsername(), "用户账号不能为空");

        // 构建授权中心创建请求
        CreateUserRequest oauthRequest = buildOauthCreateRequest(request);

        // 调用授权中心注册基础账号，获取 userId
        String userId = userService.create(oauthRequest);
        log.info("授权中心创建用户成功，userId={}", userId);

        // 构建本地用户实体并落库
        AiUser entity = buildAiUserEntity(request, userId);
        aiUserView.save(entity);
        log.info("本地 ai_user 创建成功，userId={}", userId);

        return userId;
    }

    @Override
    public void update(UpdateAiUserRequest request) {

        // 参数校验：用户ID不能为空
        AssertUtils.notEmpty(request.getId(), "用户ID不能为空");

        // 查询本地用户记录
        AiUser entity = aiUserView.findById(request.getId());
        AssertUtils.notEmpty(entity, "用户[{}]不存在", request.getId());

        // 更新本地业务字段
        applyLocalUpdate(entity, request);
        aiUserView.updateById(entity);

        // 同步更新授权中心基础信息
        syncOauthUpdate(request);
    }

    @Override
    public void delete(String id) {

        // 参数校验：用户ID不能为空
        AssertUtils.notEmpty(id, "用户ID不能为空");

        // 删除本地用户记录
        aiUserView.deleteById(id);

        // 删除授权中心基础账号
        userService.delete(Collections.singletonList(id));
        log.info("删除用户成功，userId={}", id);
    }

    /**
     * 从授权中心补充用户基础信息。
     *
     * @param response 本地响应对象（将被修改）
     * @param userId   用户主键
     */
    private void augmentFromOauth(InfoAiUserResponse response, String userId) {

        // 从授权中心查询基础信息
        UserInfoResponse oauthUser = userService.findById(userId);
        if (oauthUser != null) {
            response.setUsername(oauthUser.getUsername());
            response.setPhone(oauthUser.getPhone());
        }
    }

    /**
     * 构建授权中心创建用户请求。
     *
     * @param request 本地创建请求
     * @return 授权中心创建请求
     */
    private CreateUserRequest buildOauthCreateRequest(CreateAiUserRequest request) {
        CreateUserRequest oauthRequest = new CreateUserRequest();
        oauthRequest.setNickname(request.getNickname());
        oauthRequest.setUsername(request.getUsername());
        oauthRequest.setPhone(request.getPhone());
        oauthRequest.setAvatarUrl(request.getAvatarUrl());
        return oauthRequest;
    }

    /**
     * 构建本地 ai_user 实体。
     *
     * @param request 创建请求
     * @param userId  授权中心返回的用户主键
     * @return ai_user 实体
     */
    private AiUser buildAiUserEntity(CreateAiUserRequest request, String userId) {
        AiUser entity = new AiUser();
        entity.setId(userId);
        entity.setNickname(request.getNickname());
        entity.setAvatarUrl(request.getAvatarUrl());

        // 设置 AI 配额，未指定时默认 100
        if (request.getDailyQuota() != null) {
            entity.setDailyQuota(request.getDailyQuota());
        } else {
            entity.setDailyQuota(100);
        }

        entity.setUsedQuota(0);
        entity.setPreferences(request.getPreferences());
        entity.setRemark(request.getRemark());
        return entity;
    }

    /**
     * 更新本地 ai_user 业务字段。
     *
     * @param entity  现有用户实体
     * @param request 更新请求
     */
    private void applyLocalUpdate(AiUser entity, UpdateAiUserRequest request) {

        // 逐字段更新，禁止链式 setter
        entity.setNickname(request.getNickname());
        entity.setAvatarUrl(request.getAvatarUrl());

        if (request.getDailyQuota() != null) {
            entity.setDailyQuota(request.getDailyQuota());
        }

        entity.setPreferences(request.getPreferences());
        entity.setRemark(request.getRemark());
    }

    /**
     * 同步更新授权中心用户基础信息。
     *
     * @param request 更新请求
     */
    private void syncOauthUpdate(UpdateAiUserRequest request) {
        UpdateUserRequest oauthRequest = new UpdateUserRequest();
        oauthRequest.setId(request.getId());
        oauthRequest.setNickname(request.getNickname());
        oauthRequest.setUsername(request.getUsername());
        oauthRequest.setPhone(request.getPhone());
        oauthRequest.setAvatarUrl(request.getAvatarUrl());
        userService.update(oauthRequest);
    }
}
