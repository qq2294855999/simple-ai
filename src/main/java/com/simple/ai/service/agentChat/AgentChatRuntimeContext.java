package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agent.AgentContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体聊天运行时上下文。
 * <p>不可变对象，持有单轮聊天所需的全部运行时事实，显式向下传递。</p>
 * <p>替代原有的 ThreadLocal 隐式传递方案（AgentSessionContextHolder、ProgressConsumerHolder 等）。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public final class AgentChatRuntimeContext {

    /**
     * 会话主键
     */
    private final String sessionId;

    /**
     * 轮次主键
     */
    private final String turnId;

    /**
     * 用户主键
     */
    private final String userId;

    /**
     * 智能体定义主键
     */
    private final String agentId;

    /**
     * 模型主键
     */
    private final String modelId;

    /**
     * 客户端主键
     */
    private final String clientId;

    /**
     * 当前任务主键
     */
    private final String taskId;

    /**
     * 从会话快照恢复的智能体上下文（定义、规则、技能、记忆、客户端、执行器、协议等）
     */
    private final AgentContext agentContext;

    /**
     * 统一事件发送器
     */
    private final ChatEventSender eventSender;
}