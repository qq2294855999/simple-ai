package com.simple.ai.common.service.agentMemoryStep;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemoryStep.CreateAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepResponse;
import com.simple.ai.common.dto.agentMemoryStep.UpdateAgentMemoryStepRequest;

import java.util.List;

/**
 * 智能体记忆步骤(agent_memory_step)接口。
 *
 * @author qty
 */
public interface AgentMemoryStepService {

    /**
     * 分页列表。
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentMemoryStepResponse> findAll(PageAgentMemoryStepRequest pageRequest);

    /**
     * 新增步骤。
     *
     * @param createRequest 创建请求
     * @return 新增记录主键
     */
    String save(CreateAgentMemoryStepRequest createRequest);

    /**
     * 根据主键修改步骤。
     *
     * @param updateRequest 修改请求
     * @return 修改记录主键
     */
    String updateById(UpdateAgentMemoryStepRequest updateRequest);

    /**
     * 删除步骤。
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);
}