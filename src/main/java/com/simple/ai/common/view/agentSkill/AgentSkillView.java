package com.simple.ai.common.view.agentSkill;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillResponse;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.FindOneAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.FindAllAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.DeleteAgentSkillRequest;

/**
 * 智能体技能(agent_skill)数据库视图接口
 *
 * @author qty
 */
public interface AgentSkillView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentSkill> findAll(PageAgentSkillRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 分页参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentSkillResponse> findAggregateAll(PageAggregateAgentSkillRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentSkill 原始表数据
     */
    List<AgentSkill> findAll(FindAllAgentSkillRequest findAllRequest, FindAllAgentSkillRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentSkill 原始表数据
     */
    AgentSkill findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentSkill 原始表数据
     */
    AgentSkill findOne(FindOneAgentSkillRequest findOneRequest, FindOneAgentSkillRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAgentSkillRequest findOneRequest, FindOneAgentSkillRequest neRequest);

    /**
     * 新增
     *
     * @param agentSkill 智能体技能对象
     */
    void save(AgentSkill agentSkill);

    /**
     * 根据id修改
     *
     * @param agentSkill 智能体技能对象
     */
    void updateById(AgentSkill agentSkill);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AgentSkill> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentSkill> list);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 删除
     *
     * @param request 条件
     */
    void delete(DeleteAgentSkillRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentSkill 原始表数据
     */
    default AgentSkill findOne(FindOneAgentSkillRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentSkillRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentSkill 原始表数据
     */
    default List<AgentSkill> findAll(FindAllAgentSkillRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentSkillRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAgentSkillRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentSkillRequest());
    }

}

