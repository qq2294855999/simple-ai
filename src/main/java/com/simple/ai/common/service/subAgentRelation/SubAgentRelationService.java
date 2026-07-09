package com.simple.ai.common.service.subAgentRelation;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;

/**
 * 子智能体关联(sub_agent_relation)接口
 *
 * @author qty
 */
public interface SubAgentRelationService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageSubAgentRelationResponse> findAll(PageSubAgentRelationRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return SubAgentRelationFullInfoResponse  子智能体关联 详细数据
     */
    InfoSubAgentRelationResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 子智能体关联 请求对象
     */
    String save(CreateSubAgentRelationRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 子智能体关联 请求对象
     */
    String updateById(UpdateSubAgentRelationRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

