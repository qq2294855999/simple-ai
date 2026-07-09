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
import java.util.List;

/**
 * 智能体记忆匹配器。
 *
 * @author qty
 */
@Component
public class AgentMemoryMatcher {

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

        // 按触发条件过滤候选记忆
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
        List<AgentMemory> result = new ArrayList<>();

        // 遍历记忆列表，保留触发条件命中的记忆
        for (AgentMemory memory : memories) {
            if (isMatched(memory, commandContent)) {
                result.add(memory);
            }
        }
        return result;
    }

    /**
     * 判断记忆是否命中当前命令。
     *
     * @param memory 智能体记忆
     * @param commandContent 命令内容
     * @return 是否命中
     */
    private boolean isMatched(AgentMemory memory, String commandContent) {
        String triggerCondition = memory.getTriggerCondition();

        // 触发条件为空时不参与自动匹配
        if (triggerCondition == null || triggerCondition.isBlank()) {
            return false;
        }
        return commandContent.contains(triggerCondition);
    }
}
