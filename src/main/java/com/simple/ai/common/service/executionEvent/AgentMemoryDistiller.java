package com.simple.ai.common.service.executionEvent;

/**
 * 智能体记忆蒸馏器接口，负责从执行轨迹中提炼记忆证据。
 * <p>在每轮对话完成后异步调用，收集 ExecutionEvent 判断沉淀条件并创建 MemoryEvidence。</p>
 *
 * @author qty
 */
public interface AgentMemoryDistiller {

    /**
     * 对指定轮次的执行轨迹进行记忆蒸馏。
     * <p>收集该轮次所有执行事件，判断是否适合沉淀为记忆证据，若适合则创建 MemoryEvidence 记录。</p>
     *
     * @param turnId         对话轮次主键
     * @param taskId         调度任务主键
     * @param agentId        智能体主键
     * @param commandContent 用户原始命令内容
     */
    void distill(String turnId, String taskId, String agentId, String commandContent);
}
