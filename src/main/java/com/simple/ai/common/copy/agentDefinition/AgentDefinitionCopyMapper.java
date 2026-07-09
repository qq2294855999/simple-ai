package com.simple.ai.common.copy.agentDefinition;

import java.util.Date;

import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;

import java.util.List;

/**
 * 智能体定义(agent_definition)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentDefinitionCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentDefinitionResponse toPageResponse(AgentDefinition entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentDefinitionResponse toInfoResponse(AgentDefinition entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentDefinition 数据对象
     */
    AgentDefinition toEntity(CreateAgentDefinitionRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentDefinition 数据对象
     */
    AgentDefinition toEntity(UpdateAgentDefinitionRequest updateRequest);

}

