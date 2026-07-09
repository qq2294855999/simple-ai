package com.simple.ai.common.service.agentSkill;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;

/**
 * 智能体技能(agent_skill)接口
 *
 * @author qty
 */
public interface AgentSkillService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentSkillResponse> findAll(PageAgentSkillRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentSkillFullInfoResponse  智能体技能 详细数据
     */
    InfoAgentSkillResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体技能 请求对象
     */
    String save(CreateAgentSkillRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体技能 请求对象
     */
    String updateById(UpdateAgentSkillRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

