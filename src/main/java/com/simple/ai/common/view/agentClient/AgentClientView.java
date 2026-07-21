package com.simple.ai.common.view.agentClient;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentClient.PageAgentClientRequest;
import com.simple.ai.common.dto.agentClient.PageAgentClientResponse;
import com.simple.ai.common.entity.agentClient.AgentClient;

import java.util.List;

/**
 * 客户端实例(agent_client)数据库视图接口。
 *
 * @author qty
 */
public interface AgentClientView {

    /**
     * 分页查询客户端实例列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentClientResponse> findAll(PageAgentClientRequest pageRequest, Page<PageAgentClientResponse> page);

    /**
     * 按主键查询客户端实例。
     *
     * @param id 主键
     * @return 客户端实例实体，不存在返回 null
     */
    AgentClient findById(String id);

    /**
     * 按主键查询客户端实例（带 FOR UPDATE 行锁）。
     * <p>用于 WebSocket 鉴权等并发场景，防止竞态条件。</p>
     *
     * @param id 主键
     * @return 客户端实例实体，不存在返回 null
     */
    AgentClient findByIdWithLock(String id);

    /**
     * 保存客户端实例。
     *
     * @param entity 客户端实例实体
     */
    void save(AgentClient entity);

    /**
     * 按主键更新客户端实例。
     *
     * @param entity 客户端实例实体
     */
    void updateById(AgentClient entity);

    /**
     * 按主键删除客户端实例。
     *
     * @param id 主键
     */
    void deleteById(String id);
}
