package com.simple.ai.common.copy.agentExecutor;

import com.simple.ai.common.dto.agentExecutor.CreateAgentExecutorRequest;
import com.simple.ai.common.dto.agentExecutor.InfoAgentExecutorResponse;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorResponse;
import com.simple.ai.common.dto.agentExecutor.UpdateAgentExecutorRequest;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import org.mapstruct.Mapper;

/**
 * 执行器类型(agent_executor)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentExecutorCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentExecutorResponse toPageResponse(AgentExecutor entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentExecutorResponse toInfoResponse(AgentExecutor entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentExecutor 数据对象
     */
    AgentExecutor toEntity(CreateAgentExecutorRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentExecutor 数据对象
     */
    AgentExecutor toEntity(UpdateAgentExecutorRequest updateRequest);

}
