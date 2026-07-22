package com.simple.ai.common.service.agentChat;

import com.simple.ai.common.dto.agentChat.*;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.entity.taskDetail.TaskDetail;

import java.util.List;
import java.util.function.Consumer;

/**
 * 智能体聊天服务接口。
 *
 * @author qty
 */
public interface AgentChatService {

    /**
     * 创建聊天会话。
     *
     * @param request 创建会话请求
     * @return 会话响应
     */
    AgentChatSessionResponse createSession(CreateAgentChatSessionRequest request);

    /**
     * 查询智能体下的会话列表。
     *
     * @param agentId 智能体主键
     * @return 会话列表
     */
    List<AgentChatSessionResponse> findSessions(String agentId);

    /**
     * 查询会话历史消息（全量）。
     *
     * @param sessionId 会话主键
     * @return 消息列表
     */
    List<AgentChatMessageResponse> findMessages(String sessionId);

    /**
     * 分页查询会话历史消息（按序号倒序，用于滚动向上加载更早的消息）。
     *
     * @param sessionId 会话主键
     * @param size 每页数量
     * @param beforeSequenceNo 不包含此序号（首次传 Long.MAX_VALUE）
     * @return 消息列表（按序号升序）
     */
    List<AgentChatMessageResponse> findMessages(String sessionId, int size, long beforeSequenceNo);

    /**
     * 流式发送聊天消息。
     *
     * @param request 发送消息请求
     * @param eventConsumer 进度事件消费者
     */
    void sendStream(SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer);

    /**
     * 删除单个会话及其关联的消息和任务轨迹。
     *
     * @param sessionId 会话主键
     */
    void deleteSession(String sessionId);

    /**
     * 批量删除会话及其关联的消息和任务轨迹。
     *
     * @param sessionIds 会话主键列表
     */
    void deleteSessions(List<String> sessionIds);

    /**
     * 查询会话的执行轨迹（任务详情）。
     *
     * @param sessionId 会话主键
     * @return 任务详情列表
     */
    List<TaskDetail> findTrajectory(String sessionId);

    /**
     * 查询轮次状态，用于断线重连时判断轮次是否已完成。
     *
     * @param turnId 轮次主键
     * @return 轮次状态响应
     */
    AgentChatTurnStatusResponse findTurnStatus(String turnId);
}
