package com.simple.ai.view.agentMemoryVersion;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionRequest;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionResponse;
import com.simple.ai.common.entity.agentMemoryVersion.AgentMemoryVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 记忆版本(agent_memory_version)数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentMemoryVersionRepository extends BaseMapper<AgentMemoryVersion> {

    /**
     * 分页查询记忆版本列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentMemoryVersionResponse> selectPage(@Param("pageRequest") PageAgentMemoryVersionRequest pageRequest, Page<PageAgentMemoryVersionResponse> page);
}
