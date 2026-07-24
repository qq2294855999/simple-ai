package com.simple.ai.common.service.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.*;

import java.util.List;

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
     * 获取单条数据
     *
     * @param id 主键
     * @return 记忆详情
     */
    InfoAgentMemoryResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 创建请求
     * @return 新增记录主键
     */
    String save(CreateAgentMemoryRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 修改请求
     * @return 修改记录主键
     */
    String updateById(UpdateAgentMemoryRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 发布记忆版本。
     * <p>将 DRAFT 状态的记忆发布为 PUBLISHED，使其可被记忆匹配器使用。</p>
     *
     * @param id 记忆ID
     */
    void publish(String id);
}