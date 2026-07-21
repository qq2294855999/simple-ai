package com.simple.ai.common.service.agentMemoryVersion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemoryVersion.*;

import java.util.List;

/**
 * 记忆版本(agent_memory_version)服务接口。
 * <p>管理记忆版本的生命周期：草稿→已发布→已退役。</p>
 *
 * @author qty
 */
public interface AgentMemoryVersionService {

    /**
     * 分页查询记忆版本列表。
     *
     * @param pageRequest 分页查询请求
     * @return 分页结果
     */
    IPage<PageAgentMemoryVersionResponse> findAll(PageAgentMemoryVersionRequest pageRequest);

    /**
     * 根据主键查询记忆版本详情。
     *
     * @param id 主键
     * @return 记忆版本详情
     */
    InfoAgentMemoryVersionResponse findById(String id);

    /**
     * 新增记忆版本。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    String save(CreateAgentMemoryVersionRequest createRequest);

    /**
     * 更新记忆版本。
     *
     * @param updateRequest 更新请求
     */
    void updateById(UpdateAgentMemoryVersionRequest updateRequest);

    /**
     * 删除记忆版本。
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 发布记忆版本，将版本状态变更为已发布。
     *
     * @param id 主键
     */
    void publish(String id);

    /**
     * 废弃记忆版本，将版本状态变更为已退役。
     *
     * @param id 主键
     */
    void retire(String id);
}
