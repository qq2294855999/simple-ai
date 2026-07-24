package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.agent.AgentContext;

/**
 * 记忆匹配器。
 * <p>通过 AI 意图识别，将用户输入与已发布记忆进行匹配。
 * 匹配成功时返回记忆ID，否则返回空。</p>
 *
 * @author qty
 */
public interface MemoryMatcher {

    /**
     * 匹配用户输入与已发布记忆。
     * <p>将用户命令内容和候选记忆列表提交给 AI，
     * AI 判断用户意图是否与某个已发布记忆一致，返回匹配的记忆ID。</p>
     *
     * @param userInput 用户输入内容
     * @param context   智能体上下文（含候选记忆列表）
     * @return 匹配的记忆ID，未匹配时返回 null
     */
    String match(String userInput, AgentContext context);
}