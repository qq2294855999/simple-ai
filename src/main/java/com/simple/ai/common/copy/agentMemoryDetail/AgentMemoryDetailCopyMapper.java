package com.simple.ai.common.copy.agentMemoryDetail;

import java.util.Date;

import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.InfoAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.UpdateAgentMemoryDetailRequest;

import java.util.List;

/**
 * 智能体记忆详情(agent_memory_detail)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentMemoryDetailCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentMemoryDetailResponse toPageResponse(AgentMemoryDetail entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentMemoryDetailResponse toInfoResponse(AgentMemoryDetail entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentMemoryDetail 数据对象
     */
    AgentMemoryDetail toEntity(CreateAgentMemoryDetailRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentMemoryDetail 数据对象
     */
    AgentMemoryDetail toEntity(UpdateAgentMemoryDetailRequest updateRequest);

}

