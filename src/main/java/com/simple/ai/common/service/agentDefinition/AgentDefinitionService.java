package com.simple.ai.common.service.agentDefinition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;

import java.util.List;

/**
 * 智能体定义(agent_definition)接口
 *
 * @author qty
 */
public interface AgentDefinitionService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentDefinitionResponse> findAll(PageAgentDefinitionRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 请求参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentDefinitionResponse> findAggregateAll(PageAggregateAgentDefinitionRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return 智能体定义详细数据
     */
    InfoAgentDefinitionResponse findById(String id);

    /**
     * 获取聚合详情。
     *
     * @param id 主键
     * @return 智能体定义聚合详情
     */
    InfoAggregateAgentDefinitionResponse findAggregateById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体定义请求对象
     * @return 主键
     */
    String save(CreateAgentDefinitionRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体定义请求对象
     * @return 主键
     */
    String updateById(UpdateAgentDefinitionRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 级联删除智能体定义。
     *
     * @param ids 主键列表
     * @return 删除影响统计
     */
    DeleteCascadeAgentDefinitionResponse deleteCascadeByIds(List<String> ids);
}

