package com.simple.ai.view.agentSkill;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

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

}

