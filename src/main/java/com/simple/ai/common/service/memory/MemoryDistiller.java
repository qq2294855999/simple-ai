package com.simple.ai.common.service.memory;

/**
 * 记忆蒸馏器。
 * <p>AI 探索成功后，从 task + task_details 提炼执行轨迹，
 * 识别参数占位符，创建 agent_memory (DRAFT) + agent_memory_step × N。
 * 复用当前会话的 AI 模型完成参数识别和步骤提炼。</p>
 *
 * @author qty
 */
public interface MemoryDistiller {

    /**
     * 从任务执行轨迹蒸馏记忆。
     * <p>读取 task 及其 task_details，通过 AI 提炼最短执行链，
     * 识别可参数化的占位符，创建记忆草稿和步骤序列。</p>
     *
     * @param taskId 来源任务ID
     */
    void distill(String taskId);
}