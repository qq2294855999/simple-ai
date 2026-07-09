package com.simple.ai.common.service.subAgentAssociation;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.InfoSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.CreateSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.UpdateSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationRequest;

/**
 * 子智能体关联(sub_agent_association)接口
 *
 * @author qty
 */
public interface SubAgentAssociationService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageSubAgentAssociationResponse> findAll(PageSubAgentAssociationRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return SubAgentAssociationFullInfoResponse  子智能体关联 详细数据
     */
    InfoSubAgentAssociationResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 子智能体关联 请求对象
     */
    String save(CreateSubAgentAssociationRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 子智能体关联 请求对象
     */
    String updateById(UpdateSubAgentAssociationRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

