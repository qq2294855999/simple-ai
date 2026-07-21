package com.simple.ai.common.copy.agentMemoryVersionDetail;

import com.simple.ai.common.dto.agentMemoryVersionDetail.CreateAgentMemoryVersionDetailRequest;
import com.simple.ai.common.dto.agentMemoryVersionDetail.InfoAgentMemoryVersionDetailResponse;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailResponse;
import com.simple.ai.common.dto.agentMemoryVersionDetail.UpdateAgentMemoryVersionDetailRequest;
import com.simple.ai.common.entity.agentMemoryVersionDetail.AgentMemoryVersionDetail;
import org.mapstruct.Mapper;

/**
 * 记忆版本步骤(agent_memory_version_detail)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentMemoryVersionDetailCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentMemoryVersionDetailResponse toPageResponse(AgentMemoryVersionDetail entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentMemoryVersionDetailResponse toInfoResponse(AgentMemoryVersionDetail entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentMemoryVersionDetail 数据对象
     */
    AgentMemoryVersionDetail toEntity(CreateAgentMemoryVersionDetailRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentMemoryVersionDetail 数据对象
     */
    AgentMemoryVersionDetail toEntity(UpdateAgentMemoryVersionDetailRequest updateRequest);

}
