package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.agentChat.ChatSseEvent;

import java.util.function.Consumer;

/**
 * 记忆重新探索服务接口。
 * <p>当记忆执行失败后，由 Web 端触发 AI 重新探索更优的命令序列，
 * 探索过程通过 SSE 流式透传进度，AI 产出新步骤后通过 reviseMemory 工具覆盖原记忆。</p>
 *
 * @author qty
 */
public interface MemoryReExploreService {

    /**
     * 对失败任务关联的记忆进行重新探索。
     * <p>校验任务状态为 FAILED 且关联记忆，读取失败步骤和原记忆上下文，
     * 构造探索 prompt 后委托 AI 重新探索，进度通过事件消费者流式透传。</p>
     *
     * @param taskId        失败任务主键
     * @param eventConsumer SSE 事件消费者
     */
    void reExplore(String taskId, Consumer<ChatSseEvent> eventConsumer);
}
