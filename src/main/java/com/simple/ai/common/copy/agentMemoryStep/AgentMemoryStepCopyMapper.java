package com.simple.ai.common.copy.agentMemoryStep;

import com.simple.ai.common.dto.agentMemoryStep.CreateAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepResponse;
import com.simple.ai.common.dto.agentMemoryStep.UpdateAgentMemoryStepRequest;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import org.mapstruct.Mapper;

/**
 * 智能体记忆步骤(agent_memory_step)对象属性复制。
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentMemoryStepCopyMapper {

    /**
     * 将数据对象赋值到page返回对象。
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentMemoryStepResponse toPageResponse(AgentMemoryStep entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象。
     *
     * @param createRequest 创建接收数据对象
     * @return AgentMemoryStep 数据对象
     */
    AgentMemoryStep toEntity(CreateAgentMemoryStepRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象。
     *
     * @param updateRequest 修改接收数据对象
     * @return AgentMemoryStep 数据对象
     */
    AgentMemoryStep toEntity(UpdateAgentMemoryStepRequest updateRequest);
}