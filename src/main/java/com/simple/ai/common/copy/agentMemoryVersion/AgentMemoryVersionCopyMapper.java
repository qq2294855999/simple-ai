package com.simple.ai.common.copy.agentMemoryVersion;

import com.simple.ai.common.dto.agentMemoryVersion.CreateAgentMemoryVersionRequest;
import com.simple.ai.common.dto.agentMemoryVersion.InfoAgentMemoryVersionResponse;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionResponse;
import com.simple.ai.common.dto.agentMemoryVersion.UpdateAgentMemoryVersionRequest;
import com.simple.ai.common.entity.agentMemoryVersion.AgentMemoryVersion;
import org.mapstruct.Mapper;

/**
 * 记忆版本(agent_memory_version)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentMemoryVersionCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentMemoryVersionResponse toPageResponse(AgentMemoryVersion entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentMemoryVersionResponse toInfoResponse(AgentMemoryVersion entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentMemoryVersion 数据对象
     */
    AgentMemoryVersion toEntity(CreateAgentMemoryVersionRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentMemoryVersion 数据对象
     */
    AgentMemoryVersion toEntity(UpdateAgentMemoryVersionRequest updateRequest);

}
