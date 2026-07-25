package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.agent.AgentContext;

/**
 * 记忆匹配器。
 *
 * <p>通过 AI 意图识别，将用户输入与已发布记忆进行匹配。
 * 匹配成功时返回记忆ID和从用户输入中提取的参数值，
 * 否则返回空结果。AI 在同一次调用中完成意图识别和参数提取，
 * 避免多次 AI 调用带来的延迟和成本。</p>
 *
 * @author qty
 */
public interface MemoryMatcher {

    /**
     * 匹配用户输入与已发布记忆，并提取参数值。
     *
     * <p>将用户命令内容和候选记忆列表提交给 AI，
     * AI 判断用户意图是否与某个已发布记忆一致，
     * 同时从用户输入中提取 params_definition 定义的参数值。</p>
     *
     * @param userInput 用户输入内容
     * @param context   智能体上下文（含候选记忆列表）
     * @return 匹配结果（含记忆ID和提取的参数），未匹配时 memoryId 为 null
     */
    MemoryMatchResult match(String userInput, AgentContext context);
}