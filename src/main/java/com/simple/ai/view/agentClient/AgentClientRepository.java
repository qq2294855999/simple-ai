package com.simple.ai.view.agentClient;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentClient.PageAgentClientRequest;
import com.simple.ai.common.dto.agentClient.PageAgentClientResponse;
import com.simple.ai.common.entity.agentClient.AgentClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户端实例(agent_client)数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentClientRepository extends BaseMapper<AgentClient> {

    /**
     * 分页查询客户端实例列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentClientResponse> selectPage(@Param("pageRequest") PageAgentClientRequest pageRequest, Page<PageAgentClientResponse> page);

}
