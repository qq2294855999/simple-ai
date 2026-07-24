package com.simple.ai.view.agentMemory;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体记忆(agent_memory)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AgentMemoryRepository extends BaseMapper<AgentMemory> {

    /**
     * 批量新增数据
     *
     * @param entities 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AgentMemory> entities);
}