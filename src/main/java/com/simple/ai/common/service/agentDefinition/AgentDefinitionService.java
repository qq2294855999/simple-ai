package com.simple.ai.common.service.agentDefinition;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;

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
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentDefinitionFullInfoResponse  智能体定义 详细数据
     */
    InfoAgentDefinitionResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体定义 请求对象
     */
    String save(CreateAgentDefinitionRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体定义 请求对象
     */
    String updateById(UpdateAgentDefinitionRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

