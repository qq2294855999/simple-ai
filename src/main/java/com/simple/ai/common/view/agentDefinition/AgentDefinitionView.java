package com.simple.ai.common.view.agentDefinition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentDefinition.DeleteAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.FindAllAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.InfoAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;

import java.util.List;

/**
 * 智能体定义(agent_definition)数据库视图接口
 *
 * @author qty
 */
public interface AgentDefinitionView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentDefinition> findAll(PageAgentDefinitionRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 分页参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentDefinitionResponse> findAggregateAll(PageAggregateAgentDefinitionRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentDefinition 原始表数据
     */
    List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest, FindAllAgentDefinitionRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentDefinition 原始表数据
     */
    AgentDefinition findById(String id);

    /**
     * 获取聚合详情基础信息。
     *
     * @param id 主键
     * @return 聚合详情基础信息
     */
    InfoAggregateAgentDefinitionResponse findAggregateById(String id);

    /**
     * 查询智能体技能。
     *
     * @param agentId 智能体主键
     * @return 技能列表
     */
    List<PageAgentSkillResponse> findSkillsByAgentId(String agentId);

    /**
     * 查询智能体规则。
     *
     * @param agentId 智能体主键
     * @return 规则列表
     */
    List<PageAgentRuleResponse> findRulesByAgentId(String agentId);

    /**
     * 查询智能体子关系。
     *
     * @param agentId 智能体主键
     * @return 子智能体关系列表
     */
    List<PageSubAgentRelationResponse> findSubAgentRelationsByAgentId(String agentId);

    /**
     * 查询智能体记忆。
     *
     * @param agentId 智能体主键
     * @return 记忆列表
     */
    List<PageAgentMemoryResponse> findMemoriesByAgentId(String agentId);

    /**
     * 查询智能体记忆详情。
     *
     * @param agentId 智能体主键
     * @return 记忆详情列表
     */
    List<PageAgentMemoryDetailResponse> findMemoryDetailsByAgentId(String agentId);

    /**
     * 查询智能体任务。
     *
     * @param agentId 智能体主键
     * @return 任务列表
     */
    List<PageTaskResponse> findTasksByAgentId(String agentId);

    /**
     * 查询智能体原子命令。
     *
     * @param agentId 智能体主键
     * @return 原子命令列表
     */
    List<PageAtomicCommandResponse> findAtomicCommandsByAgentId(String agentId);

    /**
     * 预统计级联删除影响范围。
     *
     * @param ids 智能体主键列表
     * @return 删除影响统计
     */
    DeleteCascadeAgentDefinitionResponse countCascadeByIds(List<String> ids);

    /**
     * 解除智能体技能关联的原子命令。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int unlinkAtomicCommandSkillByAgentIds(List<String> ids);

    /**
     * 删除任务详情。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteTaskDetailByAgentIds(List<String> ids);

    /**
     * 删除任务。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteTaskByAgentIds(List<String> ids);

    /**
     * 删除记忆详情。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteMemoryDetailByAgentIds(List<String> ids);

    /**
     * 删除记忆。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteMemoryByAgentIds(List<String> ids);

    /**
     * 删除子智能体关系。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteSubAgentRelationByAgentIds(List<String> ids);

    /**
     * 删除规则。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteRuleByAgentIds(List<String> ids);

    /**
     * 删除技能。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteSkillByAgentIds(List<String> ids);

    /**
     * 删除智能体聊天消息。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteChatMessageByAgentIds(List<String> ids);

    /**
     * 删除智能体聊天会话。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteChatSessionByAgentIds(List<String> ids);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentDefinition 原始表数据
     */
    AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest);

    /**
     * 新增
     *
     * @param agentDefinition 智能体定义对象
     */
    void save(AgentDefinition agentDefinition);

    /**
     * 根据id修改
     *
     * @param agentDefinition 智能体定义对象
     */
    void updateById(AgentDefinition agentDefinition);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AgentDefinition> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentDefinition> list);

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
    void delete(DeleteAgentDefinitionRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentDefinition 原始表数据
     */
    default AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentDefinitionRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentDefinition 原始表数据
     */
    default List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentDefinitionRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAgentDefinitionRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentDefinitionRequest());
    }
}
