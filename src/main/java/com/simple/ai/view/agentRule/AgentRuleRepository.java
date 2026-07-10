package com.simple.ai.view.agentRule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.entity.agentRule.AgentRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体规则(agent_rule)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AgentRuleRepository extends BaseMapper<AgentRule> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<AgentRule> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AgentRule> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateAgentRuleResponse> selectAggregatePage(@Param("pageRequest") PageAggregateAgentRuleRequest pageRequest,
                                                             @Param("offset") Long offset,
                                                             @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateAgentRuleRequest pageRequest);
}

