package com.simple.ai.common.copy.agentClient;

import com.simple.ai.common.dto.agentClient.CreateAgentClientRequest;
import com.simple.ai.common.dto.agentClient.InfoAgentClientResponse;
import com.simple.ai.common.dto.agentClient.PageAgentClientResponse;
import com.simple.ai.common.dto.agentClient.UpdateAgentClientRequest;
import com.simple.ai.common.entity.agentClient.AgentClient;
import org.mapstruct.Mapper;

/**
 * 客户端实例(agent_client)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentClientCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentClientResponse toPageResponse(AgentClient entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentClientResponse toInfoResponse(AgentClient entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentClient 数据对象
     */
    AgentClient toEntity(CreateAgentClientRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentClient 数据对象
     */
    AgentClient toEntity(UpdateAgentClientRequest updateRequest);

}
