package com.simple.ai.common.view.agentMemoryVersionDetail;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailRequest;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailResponse;
import com.simple.ai.common.entity.agentMemoryVersionDetail.AgentMemoryVersionDetail;

import java.util.List;

/**
 * 记忆版本步骤(agent_memory_version_detail)数据库视图接口。
 *
 * @author qty
 */
public interface AgentMemoryVersionDetailView {

    /**
     * 分页查询记忆版本步骤列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentMemoryVersionDetailResponse> findAll(PageAgentMemoryVersionDetailRequest pageRequest, Page<PageAgentMemoryVersionDetailResponse> page);

    /**
     * 按主键查询记忆版本步骤。
     *
     * @param id 主键
     * @return 记忆版本步骤实体，不存在返回 null
     */
    AgentMemoryVersionDetail findById(String id);

    /**
     * 保存记忆版本步骤。
     *
     * @param entity 记忆版本步骤实体
     */
    void save(AgentMemoryVersionDetail entity);

    /**
     * 按主键更新记忆版本步骤。
     *
     * @param entity 记忆版本步骤实体
     */
    void updateById(AgentMemoryVersionDetail entity);

    /**
     * 按主键删除记忆版本步骤。
     *
     * @param id 主键
     */
    void deleteById(String id);
}
