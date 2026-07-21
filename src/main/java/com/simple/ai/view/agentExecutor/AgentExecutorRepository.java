package com.simple.ai.view.agentExecutor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorRequest;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorResponse;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 执行器类型(agent_executor)数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentExecutorRepository extends BaseMapper<AgentExecutor> {

    /**
     * 分页查询执行器类型列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentExecutorResponse> selectPage(@Param("pageRequest") PageAgentExecutorRequest pageRequest, Page<PageAgentExecutorResponse> page);
}
