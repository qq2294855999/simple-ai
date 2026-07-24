package com.simple.ai.view.agentMemoryStep;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体记忆步骤(agent_memory_step)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AgentMemoryStepRepository extends BaseMapper<AgentMemoryStep> {

    /**
     * 批量新增数据
     *
     * @param entities 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AgentMemoryStep> entities);
}