package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.agentMemory.CommandStep;

import java.util.List;

/**
 * 记忆蒸馏器。
 * <p>AI 探索成功后，从 task + task_details 提炼执行轨迹，
 * 识别参数占位符，创建 agent_memory + agent_memory_step × N。
 * 复用当前会话的 AI 模型完成参数识别和步骤提炼。</p>
 *
 * @author qty
 */
public interface MemoryDistiller {

    /**
     * 从任务执行轨迹蒸馏记忆。
     * <p>读取 task 及其 task_details，通过 AI 提炼最短执行链，
     * 识别可参数化的占位符，创建记忆和步骤序列。</p>
     *
     * @param taskId 来源任务ID
     */
    void distill(String taskId);

    /**
     * 覆盖式修订记忆。
     * <p>删除原记忆的所有步骤，按传入命令序列重新构建步骤，
     * 覆盖记忆名称和参数定义，保留 id/agentId/clientId/userId。</p>
     *
     * @param memoryId             被修订的记忆ID
     * @param steps                新命令序列
     * @param paramsDefinitionJson 参数定义JSON
     * @param memoryNameHint       记忆名称提示（为空时保留原名）
     */
    void distillRevision(String memoryId, List<CommandStep> steps, String paramsDefinitionJson, String memoryNameHint);
}