package com.simple.ai.common.copy.subAgentRelation;

import java.util.Date;

import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;

import java.util.List;

/**
 * 子智能体关联(sub_agent_relation)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface SubAgentRelationCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageSubAgentRelationResponse toPageResponse(SubAgentRelation entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoSubAgentRelationResponse toInfoResponse(SubAgentRelation entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return SubAgentRelation 数据对象
     */
    SubAgentRelation toEntity(CreateSubAgentRelationRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return SubAgentRelation 数据对象
     */
    SubAgentRelation toEntity(UpdateSubAgentRelationRequest updateRequest);

}

