package com.simple.ai.service.memory;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.service.memory.MemoryMatchResult;
import com.simple.ai.common.service.memory.MemoryMatcher;
import com.simple.common.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记忆匹配器默认实现。
 *
 * <p>通过 AI 意图识别将用户输入与已发布记忆进行匹配，
 * 同时从用户输入中提取 params_definition 定义的参数值。
 * AI 在同一次调用中完成意图识别和参数提取，
 * 避免多次 AI 调用带来的延迟和成本。</p>
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
    public MemoryMatchResult match(String userInput, AgentContext context) {
        List<AgentMemory> memories = context.getMemories();

        // 无候选记忆时直接返回空结果
        if (memories == null || memories.isEmpty()) {
            return new MemoryMatchResult();
        }

        // 构造意图识别+参数提取提示词
        String prompt = buildIntentAndExtractionPrompt(userInput, memories);

        // 调用 AI 进行意图识别和参数提取
        AgentAiRequest aiRequest = new AgentAiRequest();
        aiRequest.setPromptContent(prompt);
        aiRequest.setCommandContent(userInput);
        aiRequest.setAgentId(context.getAgentDefinition().getId());
        aiRequest.setSessionId(context.getSessionId() != null ? context.getSessionId() : "");
        aiRequest.setUserId(context.getUserId());

        AgentAiResponse aiResponse = agentAiClient.chat(aiRequest);

        // 解析 AI 返回的匹配结果和提取的参数
        return parseMatchResult(aiResponse, memories);
    }

    /**
     * 构造意图识别+参数提取提示词。
     *
     * <p>AI 需要同时完成两件事：
     * 1. 判断用户输入最匹配哪个记忆
     * 2. 从用户输入中提取该记忆 params_definition 定义的参数值</p>
     *
     * @param userInput 用户输入
     * @param memories  候选记忆列表
     * @return 提示词内容
     */
    private String buildIntentAndExtractionPrompt(String userInput, List<AgentMemory> memories) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是一个意图识别和参数提取助手。请完成以下两个任务：\n");
        builder.append("1. 判断用户输入最匹配以下哪个记忆\n");
        builder.append("2. 从用户输入中提取该记忆参数定义中声明的参数值\n\n");
        builder.append("用户输入：").append(userInput).append("\n\n");
        builder.append("候选记忆列表：\n");

        // 遍历候选记忆构造选项，包含参数定义供AI提取参数
        for (AgentMemory memory : memories) {
            builder.append("- ID: ").append(memory.getId());
            builder.append("，名称: ").append(memory.getMemoryName());
            builder.append("，摘要: ").append(memory.getSummary() != null ? memory.getSummary() : "");
            builder.append("，参数定义: ").append(memory.getParamsDefinition() != null ? memory.getParamsDefinition() : "{}");
            builder.append("\n");
        }

        builder.append("\n请按以下JSON格式返回结果，不要返回其他内容：\n");
        builder.append("{\n");
        builder.append("  \"memoryId\": \"匹配的记忆ID，不匹配时为空字符串\",\n");
        builder.append("  \"params\": {\"参数名\": \"从用户输入中提取的参数值\"}\n");
        builder.append("}\n");
        return builder.toString();
    }

    /**
     * 解析 AI 返回的匹配结果和提取的参数。
     *
     * @param aiResponse AI 响应
     * @param memories   候选记忆列表
     * @return 匹配结果（含记忆ID和提取的参数）
     */
    private MemoryMatchResult parseMatchResult(AgentAiResponse aiResponse, List<AgentMemory> memories) {
        MemoryMatchResult result = new MemoryMatchResult();

        // AI 响应为空或失败时返回空结果
        if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess()) || aiResponse.getResponseContent() == null) {
            return result;
        }

        String content = aiResponse.getResponseContent().trim();

        // 尝试解析 JSON 格式的响应
        Map<String, Object> parsed = parseJsonResponse(content);
        if (parsed != null) {
            String matchedId = extractMemoryId(parsed, memories);
            result.setMemoryId(matchedId);

            // 提取参数值
            Map<String, Object> extractedParams = extractParams(parsed);
            result.setExtractedParams(extractedParams);

            if (result.isMatched()) {
                log.info("AI 意图识别匹配记忆：memoryId={}, memoryName={}, 提取参数={}", matchedId,
                         memories.stream().filter(m -> matchedId.equals(m.getId())).map(AgentMemory::getMemoryName).findFirst().orElse(""), extractedParams);
            }
            return result;
        }

        // JSON 解析失败时回退到旧逻辑：从纯文本中匹配记忆ID
        String fallbackId = matchFromPlainText(content, memories);
        result.setMemoryId(fallbackId);
        result.setExtractedParams(new HashMap<>());

        if (fallbackId != null) {
            log.info("AI 意图识别匹配记忆（回退模式）：memoryId={}", fallbackId);
        }
        return result;
    }

    /**
     * 解析 AI 返回的 JSON 响应。
     *
     * @param content AI 响应内容
     * @return 解析后的 Map，解析失败时返回 null
     */
    private Map<String, Object> parseJsonResponse(String content) {
        try {

            // 提取 JSON 部分
            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart < 0 || jsonEnd <= jsonStart) {
                return null;
            }

            String json = content.substring(jsonStart, jsonEnd + 1);
            return JsonUtils.toJsonObj(json, Map.class);
        } catch (Exception e) {
            log.debug("AI 响应 JSON 解析失败，尝试回退模式：{}", content, e);
            return null;
        }
    }

    /**
     * 从解析结果中提取匹配的记忆ID。
     * <p>优先使用 AI 返回的 memoryId 字段，若不在候选列表中
     * 则遍历候选记忆检查 AI 返回内容是否包含其 ID。</p>
     *
     * @param parsed   AI 返回的解析结果
     * @param memories 候选记忆列表
     * @return 匹配的记忆ID，未匹配时返回 null
     */
    private String extractMemoryId(Map<String, Object> parsed, List<AgentMemory> memories) {
        Object memoryIdObj = parsed.get("memoryId");
        String memoryId = memoryIdObj != null ? memoryIdObj.toString().trim() : "";

        // AI 返回的 memoryId 为空字符串时表示未匹配
        if (memoryId.isEmpty()) {
            return null;
        }

        // 验证 AI 返回的 memoryId 是否在候选列表中
        for (AgentMemory memory : memories) {
            if (memoryId.equals(memory.getId())) {
                return memoryId;
            }
        }

        // AI 返回的 ID 不在候选列表中，尝试模糊匹配
        return matchFromPlainText(memoryId, memories);
    }

    /**
     * 从解析结果中提取参数值。
     *
     * @param parsed AI 返回的解析结果
     * @return 参数名到参数值的映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(Map<String, Object> parsed) {
        Object paramsObj = parsed.get("params");
        if (paramsObj instanceof Map) {
            return (Map<String, Object>) paramsObj;
        }
        return new HashMap<>();
    }

    /**
     * 从纯文本中匹配记忆ID（回退模式）。
     *
     * @param content  AI 响应内容
     * @param memories 候选记忆列表
     * @return 匹配的记忆ID，未匹配时返回 null
     */
    private String matchFromPlainText(String content, List<AgentMemory> memories) {

        // 遍历候选记忆匹配 AI 返回内容中包含的 ID
        for (AgentMemory memory : memories) {
            if (content.contains(memory.getId())) {
                return memory.getId();
            }
        }

        log.debug("AI 意图识别未匹配任何记忆，AI响应：{}", content);
        return null;
    }
}