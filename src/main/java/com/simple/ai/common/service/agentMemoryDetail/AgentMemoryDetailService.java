package com.simple.ai.common.service.agentMemoryDetail;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.InfoAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.UpdateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;

/**
 * 智能体记忆详情(agent_memory_detail)接口
 *
 * @author qty
 */
public interface AgentMemoryDetailService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentMemoryDetailResponse> findAll(PageAgentMemoryDetailRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentMemoryDetailFullInfoResponse  智能体记忆详情 详细数据
     */
    InfoAgentMemoryDetailResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体记忆详情 请求对象
     */
    String save(CreateAgentMemoryDetailRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体记忆详情 请求对象
     */
    String updateById(UpdateAgentMemoryDetailRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

