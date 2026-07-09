package com.simple.ai.common.copy.agentSkill;

import java.util.Date;

import com.simple.ai.common.entity.agentSkill.AgentSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;

import java.util.List;

/**
 * 智能体技能(agent_skill)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentSkillCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentSkillResponse toPageResponse(AgentSkill entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentSkillResponse toInfoResponse(AgentSkill entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentSkill 数据对象
     */
    AgentSkill toEntity(CreateAgentSkillRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentSkill 数据对象
     */
    AgentSkill toEntity(UpdateAgentSkillRequest updateRequest);

}

