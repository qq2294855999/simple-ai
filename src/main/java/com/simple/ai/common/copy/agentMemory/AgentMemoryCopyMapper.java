package com.simple.ai.common.copy.agentMemory;

import java.util.Date;

import com.simple.ai.common.entity.agentMemory.AgentMemory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.UpdateAgentMemoryRequest;

import java.util.List;

/**
 * 智能体记忆(agent_memory)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentMemoryCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentMemoryResponse toPageResponse(AgentMemory entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentMemoryResponse toInfoResponse(AgentMemory entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentMemory 数据对象
     */
    AgentMemory toEntity(CreateAgentMemoryRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentMemory 数据对象
     */
    AgentMemory toEntity(UpdateAgentMemoryRequest updateRequest);

}

