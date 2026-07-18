package com.simple.ai.common.service.aiUser;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.aiUser.CreateAiUserRequest;
import com.simple.ai.common.dto.aiUser.InfoAiUserResponse;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.dto.aiUser.UpdateAiUserRequest;

/**
 * AI 平台用户服务接口。
 * <p>
 * 负责 AI 业务域用户管理，基础认证信息委托授权中心管理。
 * 创建用户时先调用授权中心注册基础账号，获取主键后再创建本地用户记录。
 * </p>
 *
 * @author qty
 */
public interface AiUserService {

    /**
     * 分页查询用户列表。
     *
     * @param pageRequest 分页查询请求
     * @return 分页结果
     */
    IPage<PageAiUserResponse> findAll(PageAiUserRequest pageRequest);

    /**
     * 根据主键查询用户详情。
     * <p>聚合本地 ai_user 与授权中心 sys_user 的基础信息。</p>
     *
     * @param id 用户主键
     * @return 用户详情
     */
    InfoAiUserResponse findById(String id);

    /**
     * 创建用户。
     * <p>先调用授权中心 UserService.create 注册基础账号，
     * 获取 userId 后再创建本地 ai_user 记录。</p>
     *
     * @param request 创建请求
     * @return 用户主键
     */
    String create(CreateAiUserRequest request);

    /**
     * 更新用户信息。
     * <p>同步更新本地 ai_user 和授权中心 sys_user 的基础信息。</p>
     *
     * @param request 更新请求
     */
    void update(UpdateAiUserRequest request);

    /**
     * 删除用户。
     * <p>先删除本地 ai_user，再调用授权中心 UserService.delete 删除基础账号。</p>
     *
     * @param id 用户主键
     */
    void delete(String id);
}
