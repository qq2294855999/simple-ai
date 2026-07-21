package com.simple.ai.common.service.agentExecutor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentExecutor.*;

import java.util.List;

/**
 * 执行器类型(agent_executor)服务接口。
 *
 * @author qty
 */
public interface AgentExecutorService {

    /**
     * 获取 SEP v1.0 协议说明。
     * <p>返回 Simple Executor Protocol v1.0 的完整协议定义，包括消息结构、消息类型、
     * 内置系统命令和通信流程，供前端展示。</p>
     *
     * @return 协议说明响应
     */
    AgentExecutorProtocolResponse getProtocol();

    /**
     * 分页查询执行器类型列表。
     *
     * @param pageRequest 分页查询请求
     * @return 分页结果
     */
    IPage<PageAgentExecutorResponse> findAll(PageAgentExecutorRequest pageRequest);

    /**
     * 根据主键查询执行器类型详情。
     *
     * @param id 主键
     * @return 执行器类型详情
     */
    InfoAgentExecutorResponse findById(String id);

    /**
     * 新增执行器类型。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    String save(CreateAgentExecutorRequest createRequest);

    /**
     * 更新执行器类型。
     *
     * @param updateRequest 更新请求
     */
    void updateById(UpdateAgentExecutorRequest updateRequest);

    /**
     * 删除执行器类型。
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 切换执行器类型启用/停用状态。
     * <p>ENABLE 切换为 DISABLE，DISABLE 切换为 ENABLE。</p>
     *
     * @param id 主键
     * @return 切换后的状态
     */
    String toggleStatus(String id);
}
