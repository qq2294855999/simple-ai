package com.simple.ai.common.service.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.*;

import java.util.List;

/**
 * 智能体记忆(agent_memory)接口
 *
 * @author qty
 */
public interface AgentMemoryService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return 记忆详情
     */
    InfoAgentMemoryResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 创建请求
     * @return 新增记录主键
     */
    String save(CreateAgentMemoryRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 修改请求
     * @return 修改记录主键
     */
    String updateById(UpdateAgentMemoryRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 获取记忆的参数定义。
     * <p>返回参数定义JSON和步骤列表，用于前端动态生成执行表单。</p>
     *
     * @param id 记忆ID
     * @return 参数定义响应
     */
    ParamsDefinitionResponse getParamsDefinition(String id);

    /**
     * 执行记忆。
     * <p>根据用户传入的参数替换步骤模板中的占位符，创建任务并逐步骤执行。</p>
     *
     * @param id      记忆ID
     * @param request 执行请求（含参数和客户端ID）
     * @return 执行响应（含任务ID和执行状态）
     */
    ExecuteMemoryResponse execute(String id, ExecuteMemoryRequest request);
}