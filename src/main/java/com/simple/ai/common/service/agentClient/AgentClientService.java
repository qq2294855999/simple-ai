package com.simple.ai.common.service.agentClient;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentClient.*;

import java.util.List;

/**
 * 客户端实例(agent_client)服务接口。
 * <p>创建客户端时自动生成密钥（BCrypt 哈希存储），仅创建时返回一次明文。</p>
 *
 * @author qty
 */
public interface AgentClientService {

    /**
     * 分页查询客户端实例列表。
     *
     * @param pageRequest 分页查询请求
     * @return 分页结果
     */
    IPage<PageAgentClientResponse> findAll(PageAgentClientRequest pageRequest);

    /**
     * 根据主键查询客户端实例详情。
     *
     * @param id 主键
     * @return 客户端实例详情
     */
    InfoAgentClientResponse findById(String id);

    /**
     * 新增客户端实例。
     * <p>生成随机密钥，BCrypt 哈希后存储，明文仅在此次返回。</p>
     *
     * @param createRequest 创建请求
     * @param userId        用户归属ID（由后端统一赋值）
     * @return 客户端实例详情（含明文密钥）
     */
    InfoAgentClientResponse save(CreateAgentClientRequest createRequest, String userId);

    /**
     * 更新客户端实例。
     *
     * @param updateRequest 更新请求
     */
    void updateById(UpdateAgentClientRequest updateRequest);

    /**
     * 删除客户端实例。
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);
}
