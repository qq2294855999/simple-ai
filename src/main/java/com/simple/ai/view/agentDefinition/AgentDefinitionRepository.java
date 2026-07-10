package com.simple.ai.view.agentDefinition;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAggregateAgentDefinitionResponse;
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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体定义(agent_definition)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AgentDefinitionRepository extends BaseMapper<AgentDefinition> {

    /**
     * 批量新增数据。
     *
     * @param entities List<AgentDefinition> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AgentDefinition> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateAgentDefinitionResponse> selectAggregatePage(@Param("pageRequest") PageAggregateAgentDefinitionRequest pageRequest,
                                                                   @Param("offset") Long offset,
                                                                   @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateAgentDefinitionRequest pageRequest);

    /**
     * 查询聚合详情基础信息。
     *
     * @param id 主键
     * @return 聚合详情基础信息
     */
    InfoAggregateAgentDefinitionResponse selectAggregateById(@Param("id") String id);

    /**
     * 查询智能体技能。
     *
     * @param agentId 智能体主键
     * @return 技能列表
     */
    List<PageAgentSkillResponse> selectSkillsByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体规则。
     *
     * @param agentId 智能体主键
     * @return 规则列表
     */
    List<PageAgentRuleResponse> selectRulesByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体子关系。
     *
     * @param agentId 智能体主键
     * @return 子智能体关系列表
     */
    List<PageSubAgentRelationResponse> selectSubAgentRelationsByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体记忆。
     *
     * @param agentId 智能体主键
     * @return 记忆列表
     */
    List<PageAgentMemoryResponse> selectMemoriesByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体记忆详情。
     *
     * @param agentId 智能体主键
     * @return 记忆详情列表
     */
    List<PageAgentMemoryDetailResponse> selectMemoryDetailsByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体任务。
     *
     * @param agentId 智能体主键
     * @return 任务列表
     */
    List<PageTaskResponse> selectTasksByAgentId(@Param("agentId") String agentId);

    /**
     * 查询智能体原子命令。
     *
     * @param agentId 智能体主键
     * @return 原子命令列表
     */
    List<PageAtomicCommandResponse> selectAtomicCommandsByAgentId(@Param("agentId") String agentId);

    /**
     * 统计级联删除影响范围。
     *
     * @param ids 智能体主键列表
     * @return 影响范围
     */
    DeleteCascadeAgentDefinitionResponse countCascadeByIds(@Param("ids") List<String> ids);

    /**
     * 解除原子命令技能关联。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int unlinkAtomicCommandSkillByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除任务详情。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteTaskDetailByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除任务。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteTaskByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除记忆详情。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteMemoryDetailByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除记忆。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteMemoryByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除子智能体关系。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteSubAgentRelationByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除规则。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteRuleByAgentIds(@Param("ids") List<String> ids);

    /**
     * 删除技能。
     *
     * @param ids 智能体主键列表
     * @return 影响行数
     */
    int deleteSkillByAgentIds(@Param("ids") List<String> ids);
}
