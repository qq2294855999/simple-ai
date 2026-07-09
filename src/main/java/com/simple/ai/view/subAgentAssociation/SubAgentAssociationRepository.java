package com.simple.ai.view.subAgentAssociation;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.subAgentAssociation.SubAgentAssociation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 子智能体关联(sub_agent_association)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface SubAgentAssociationRepository extends BaseMapper<SubAgentAssociation> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<SubAgentAssociation> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<SubAgentAssociation> entities);

}

