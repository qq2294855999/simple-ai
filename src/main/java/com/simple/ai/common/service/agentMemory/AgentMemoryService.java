package com.simple.ai.common.service.agentMemory;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.UpdateAgentMemoryRequest;

/**
 * 智能体记忆(agent_memory)接口
 *
 * @author qty
 */
public interface AgentMemoryService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 请求参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentMemoryResponse> findAggregateAll(PageAggregateAgentMemoryRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentMemoryFullInfoResponse  智能体记忆 详细数据
     */
    InfoAgentMemoryResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体记忆 请求对象
     */
    String save(CreateAgentMemoryRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体记忆 请求对象
     */
    String updateById(UpdateAgentMemoryRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

