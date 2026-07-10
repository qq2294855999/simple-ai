package com.simple.ai.view.agentSkill;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillResponse;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体技能(agent_skill)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AgentSkillRepository extends BaseMapper<AgentSkill> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<AgentSkill> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AgentSkill> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateAgentSkillResponse> selectAggregatePage(@Param("pageRequest") PageAggregateAgentSkillRequest pageRequest,
                                                              @Param("offset") Long offset,
                                                              @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateAgentSkillRequest pageRequest);

    /**
     * 按技能主键批量解除原子命令关联。
     * 将关联的 atomic_command.skill_id 置空，保留全局命令和命令本体。
     *
     * @param skillIds 技能主键列表
     * @return 影响行数
     */
    int unlinkAtomicCommandBySkillIds(@Param("skillIds") List<String> skillIds);
}

