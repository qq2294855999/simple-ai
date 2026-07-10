package com.simple.ai.view.subAgentRelation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationResponse;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子智能体关联(sub_agent_relation)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface SubAgentRelationRepository extends BaseMapper<SubAgentRelation> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<SubAgentRelation> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<SubAgentRelation> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateSubAgentRelationResponse> selectAggregatePage(@Param("pageRequest") PageAggregateSubAgentRelationRequest pageRequest,
                                                                    @Param("offset") Long offset,
                                                                    @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateSubAgentRelationRequest pageRequest);
}

