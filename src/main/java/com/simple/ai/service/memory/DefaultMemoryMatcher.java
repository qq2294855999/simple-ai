package com.simple.ai.service.memory;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.service.memory.MemoryMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 记忆匹配器默认实现。
 * <p>通过 AI 意图识别将用户输入与已发布记忆进行匹配。
 * 构造意图识别提示词，提交给 AI 判断，返回匹配的记忆ID。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryMatcher implements MemoryMatcher {

    /**
     * AI 调用客户端
     */
    @Autowired
    private AgentAiClient agentAiClient;

    @Override
    public String match(String userInput, AgentContext context) {
        List<AgentMemory> memories = context.getMemories();

        // 无候选记忆时直接返回空
        if (memories == null || memories.isEmpty()) {
            return null;
        }

        // 构造意图识别提示词
        String prompt = buildIntentPrompt(userInput, memories);

        // 调用 AI 进行意图识别
        AgentAiRequest aiRequest = new AgentAiRequest();
        aiRequest.setPromptContent(prompt);
        aiRequest.setAgentId(context.getAgentDefinition().getId());
        aiRequest.setSessionId("");
        aiRequest.setUserId(context.getUserId());

        AgentAiResponse aiResponse = agentAiClient.chat(aiRequest);

        // 解析 AI 返回的记忆ID
        return parseMatchedMemoryId(aiResponse, memories);
    }

    /**
     * 构造意图识别提示词。
     *
     * @param userInput 用户输入
     * @param memories  候选记忆列表
     * @return 提示词内容
     */
    private String buildIntentPrompt(String userInput, List<AgentMemory> memories) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是一个意图识别助手。请判断用户输入最匹配以下哪个记忆，返回该记忆的ID。\n\n");
        builder.append("用户输入：").append(userInput).append("\n\n");
        builder.append("候选记忆列表：\n");

        // 遍历候选记忆构造选项
        for (AgentMemory memory : memories) {
            builder.append("- ID: ").append(memory.getId());
            builder.append("，名称: ").append(memory.getMemoryName());
            builder.append("，摘要: ").append(memory.getSummary() != null ? memory.getSummary() : "");
            builder.append("\n");
        }

        builder.append("\n请只返回最匹配的记忆ID，不匹配时返回空。");
        return builder.toString();
    }

    /**
     * 解析 AI 返回的匹配记忆ID。
     *
     * @param aiResponse AI 响应
     * @param memories   候选记忆列表
     * @return 匹配的记忆ID，未匹配时返回 null
     */
    private String parseMatchedMemoryId(AgentAiResponse aiResponse, List<AgentMemory> memories) {

        // AI 响应为空或失败时返回空
        if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess()) || aiResponse.getResponseContent() == null) {
            return null;
        }

        String content = aiResponse.getResponseContent().trim();

        // 遍历候选记忆匹配 AI 返回的 ID
        for (AgentMemory memory : memories) {
            if (content.contains(memory.getId())) {
                log.debug("AI 意图识别匹配记忆：memoryId={}, memoryName={}", memory.getId(), memory.getMemoryName());
                return memory.getId();
            }
        }

        log.debug("AI 意图识别未匹配任何记忆，AI响应：{}", content);
        return null;
    }
}