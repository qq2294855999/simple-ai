package com.simple.ai.common.copy.subAgentAssociation;

import java.util.Date;

import com.simple.ai.common.entity.subAgentAssociation.SubAgentAssociation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.InfoSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.CreateSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.UpdateSubAgentAssociationRequest;

import java.util.List;

/**
 * 子智能体关联(sub_agent_association)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface SubAgentAssociationCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageSubAgentAssociationResponse toPageResponse(SubAgentAssociation entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoSubAgentAssociationResponse toInfoResponse(SubAgentAssociation entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return SubAgentAssociation 数据对象
     */
    SubAgentAssociation toEntity(CreateSubAgentAssociationRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return SubAgentAssociation 数据对象
     */
    SubAgentAssociation toEntity(UpdateSubAgentAssociationRequest updateRequest);

}

