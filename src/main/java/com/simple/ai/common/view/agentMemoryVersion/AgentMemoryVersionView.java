package com.simple.ai.common.view.agentMemoryVersion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionRequest;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionResponse;
import com.simple.ai.common.entity.agentMemoryVersion.AgentMemoryVersion;

import java.util.List;

/**
 * 记忆版本(agent_memory_version)数据库视图接口。
 *
 * @author qty
 */
public interface AgentMemoryVersionView {

    /**
     * 分页查询记忆版本列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentMemoryVersionResponse> findAll(PageAgentMemoryVersionRequest pageRequest, Page<PageAgentMemoryVersionResponse> page);

    /**
     * 按主键查询记忆版本。
     *
     * @param id 主键
     * @return 记忆版本实体，不存在返回 null
     */
    AgentMemoryVersion findById(String id);

    /**
     * 保存记忆版本。
     *
     * @param entity 记忆版本实体
     */
    void save(AgentMemoryVersion entity);

    /**
     * 按主键更新记忆版本。
     *
     * @param entity 记忆版本实体
     */
    void updateById(AgentMemoryVersion entity);

    /**
     * 按主键删除记忆版本。
     *
     * @param id 主键
     */
    void deleteById(String id);
}
