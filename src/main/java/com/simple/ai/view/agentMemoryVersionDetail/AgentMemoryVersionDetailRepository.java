package com.simple.ai.view.agentMemoryVersionDetail;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailRequest;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailResponse;
import com.simple.ai.common.entity.agentMemoryVersionDetail.AgentMemoryVersionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 记忆版本步骤(agent_memory_version_detail)数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentMemoryVersionDetailRepository extends BaseMapper<AgentMemoryVersionDetail> {

    /**
     * 分页查询记忆版本步骤列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentMemoryVersionDetailResponse> selectPage(@Param("pageRequest") PageAgentMemoryVersionDetailRequest pageRequest, Page<PageAgentMemoryVersionDetailResponse> page);
}
