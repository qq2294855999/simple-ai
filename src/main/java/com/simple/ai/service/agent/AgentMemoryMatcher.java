package com.simple.ai.service.agent;

import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 智能体记忆匹配器。
 *
 * @author qty
 */
@Component
public class AgentMemoryMatcher {

    /**
     * 最低匹配分数
     */
    private static final int MIN_MATCH_SCORE = 2;

    /**
     * 完整触发条件命中分数
     */
    private static final int FULL_TRIGGER_SCORE = 10;

    /**
     * 触发词命中分数
     */
    private static final int TOKEN_TRIGGER_SCORE = 2;

    /**
     * 记忆名称命中分数
     */
    private static final int MEMORY_NAME_SCORE = 3;

    /**
     * 分隔符正则
     */
    private static final String SPLIT_REGEX = "[\\s,，;；|、]+";

    /**
     * 智能体记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 匹配智能体候选记忆。
     *
     * @param request 命令调度请求
     * @return 候选记忆列表
     */
    public List<AgentMemory> match(CommandDispatchRequest request) {

        // 参数校验：智能体ID不能为空
        AssertUtils.notEmpty(request.getAgentId(), "智能体ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 查询智能体全部启用记忆
        List<AgentMemory> memories = loadMemories(request.getAgentId());

        // 按触发条件评分过滤候选记忆
        return filterMatchedMemories(memories, request.getCommandContent());
    }

    /**
     * 查询智能体全部启用记忆。
     *
     * @param agentId 智能体ID
     * @return 智能体记忆列表
     */
    private List<AgentMemory> loadMemories(String agentId) {
        FindAllAgentMemoryRequest request = new FindAllAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);
        return agentMemoryView.findAll(request);
    }

    /**
     * 按触发条件过滤候选记忆。
     *
     * @param memories 智能体记忆列表
     * @param commandContent 命令内容
     * @return 候选记忆列表
     */
    private List<AgentMemory> filterMatchedMemories(List<AgentMemory> memories, String commandContent) {
        List<MemoryMatchResult> result = new ArrayList<>();
        String normalizedCommand = normalize(commandContent);

        // 遍历记忆列表，保留评分达到阈值的记忆
        for (AgentMemory memory : memories) {
            MemoryMatchResult matchResult = buildMatchResult(memory, normalizedCommand);
            if (matchResult.getScore() >= MIN_MATCH_SCORE) {
                result.add(matchResult);
            }
        }
        return sortAndExtractMemories(result);
    }

    /**
     * 构建单条记忆匹配结果。
     *
     * @param memory 智能体记忆
     * @param normalizedCommand 归一化命令内容
     * @return 记忆匹配结果
     */
    private MemoryMatchResult buildMatchResult(AgentMemory memory, String normalizedCommand) {
        MemoryMatchResult result = new MemoryMatchResult();
        result.setMemory(memory);
        result.setScore(calculateMatchScore(memory, normalizedCommand));
        return result;
    }

    /**
     * 计算记忆匹配分数。
     *
     * @param memory 智能体记忆
     * @param normalizedCommand 归一化命令内容
     * @return 匹配分数
     */
    private int calculateMatchScore(AgentMemory memory, String normalizedCommand) {
        int score = 0;
        String triggerCondition = normalize(memory.getTriggerCondition());

        // 触发条件为空时不参与自动匹配
        if (triggerCondition.isBlank()) {
            return score;
        }

        // 完整触发条件命中时给予最高基础分
        if (normalizedCommand.contains(triggerCondition)) {
            score = score + FULL_TRIGGER_SCORE;
        }

        // 触发条件拆分后逐词评分，增强多条件和同类短语命中能力
        score = score + calculateTokenScore(triggerCondition, normalizedCommand);

        // 记忆名称命中时补充分数，保证同名历史任务优先复用
        score = score + calculateMemoryNameScore(memory, normalizedCommand);
        return score;
    }

    /**
     * 计算触发词命中分数。
     *
     * @param triggerCondition 触发条件
     * @param normalizedCommand 归一化命令内容
     * @return 触发词命中分数
     */
    private int calculateTokenScore(String triggerCondition, String normalizedCommand) {
        int score = 0;
        String[] triggerTokens = triggerCondition.split(SPLIT_REGEX);

        // 遍历触发词，按命中的有效触发词累加分数
        for (String triggerToken : triggerTokens) {
            if (isEffectiveTokenMatched(triggerToken, normalizedCommand)) {
                score = score + TOKEN_TRIGGER_SCORE;
            }
        }
        return score;
    }

    /**
     * 判断有效触发词是否命中。
     *
     * @param triggerToken 触发词
     * @param normalizedCommand 归一化命令内容
     * @return 是否命中
     */
    private boolean isEffectiveTokenMatched(String triggerToken, String normalizedCommand) {
        String normalizedToken = normalize(triggerToken);

        // 过滤空白触发词，避免无意义命中
        if (normalizedToken.isBlank()) {
            return false;
        }
        return normalizedCommand.contains(normalizedToken);
    }

    /**
     * 计算记忆名称命中分数。
     *
     * @param memory 智能体记忆
     * @param normalizedCommand 归一化命令内容
     * @return 记忆名称命中分数
     */
    private int calculateMemoryNameScore(AgentMemory memory, String normalizedCommand) {
        String memoryName = normalize(memory.getMemoryName());

        // 记忆名称为空时不参与辅助评分
        if (memoryName.isBlank()) {
            return 0;
        }

        // 命令包含记忆名称时提升该记忆排序
        if (normalizedCommand.contains(memoryName)) {
            return MEMORY_NAME_SCORE;
        }
        return 0;
    }

    /**
     * 排序并提取候选记忆。
     *
     * @param result 记忆匹配结果列表
     * @return 候选记忆列表
     */
    private List<AgentMemory> sortAndExtractMemories(List<MemoryMatchResult> result) {
        result.sort(Comparator.comparingInt(MemoryMatchResult::getScore).reversed());
        List<AgentMemory> memories = new ArrayList<>();

        // 按匹配分数从高到低提取记忆，保证步骤链执行优先复用最相关经验
        for (MemoryMatchResult matchResult : result) {
            memories.add(matchResult.getMemory());
        }
        return memories;
    }

    /**
     * 归一化文本。
     *
     * @param text 原始文本
     * @return 归一化文本
     */
    private String normalize(String text) {

        // 文本为空时统一返回空字符串，避免调用方重复判空
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 记忆匹配结果。
     *
     * @author qty
     */
    private static class MemoryMatchResult {

        /**
         * 智能体记忆
         */
        private AgentMemory memory;

        /**
         * 匹配分数
         */
        private int score;

        /**
         * 获取智能体记忆。
         *
         * @return 智能体记忆
         */
        public AgentMemory getMemory() {
            return memory;
        }

        /**
         * 设置智能体记忆。
         *
         * @param memory 智能体记忆
         */
        public void setMemory(AgentMemory memory) {
            this.memory = memory;
        }

        /**
         * 获取匹配分数。
         *
         * @return 匹配分数
         */
        public int getScore() {
            return score;
        }

        /**
         * 设置匹配分数。
         *
         * @param score 匹配分数
         */
        public void setScore(int score) {
            this.score = score;
        }
    }
}
