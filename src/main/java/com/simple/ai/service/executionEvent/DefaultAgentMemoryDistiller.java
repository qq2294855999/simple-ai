package com.simple.ai.service.executionEvent;

import com.simple.ai.common.dto.executionEvent.FindAllExecutionEventRequest;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.entity.memoryEvidence.MemoryEvidence;
import com.simple.ai.common.service.executionEvent.AgentMemoryDistiller;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.ai.common.view.memoryEvidence.MemoryEvidenceView;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能体记忆蒸馏器默认实现。
 * <p>从执行轨迹中提炼原子命令调用链，生成结构化记忆证据并持久化。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultAgentMemoryDistiller implements AgentMemoryDistiller {

    /**
     * 执行事件视图
     */
    @Autowired
    private ExecutionEventView executionEventView;

    /**
     * 记忆证据视图
     */
    @Autowired
    private MemoryEvidenceView memoryEvidenceView;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void distill(String turnId, String taskId, String agentId, String commandContent) {
        log.debug("开始记忆蒸馏：turnId={}, taskId={}, agentId={}", turnId, taskId, agentId);

        // 加载该轮次的所有执行事件
        List<ExecutionEvent> events = loadTurnEvents(turnId);
        if (events.isEmpty()) {
            log.debug("轮次[{}]无执行事件，跳过记忆蒸馏", turnId);
            return;
        }

        // 判断是否适合沉淀为记忆证据
        if (!isSuitableForMemory(events)) {
            log.debug("轮次[{}]不满足记忆沉淀条件", turnId);
            return;
        }

        // 构建记忆证据内容
        String evidenceContent = buildEvidenceContent(events, commandContent);

        // 创建并持久化记忆证据
        MemoryEvidence evidence = createEvidence(turnId, taskId, evidenceContent);
        memoryEvidenceView.save(evidence);

        log.info("记忆证据已创建：turnId={}, evidenceId={}", turnId, evidence.getId());
    }

    /**
     * 加载指定轮次的所有执行事件。
     *
     * @param turnId 轮次主键
     * @return 执行事件列表
     */
    private List<ExecutionEvent> loadTurnEvents(String turnId) {
        FindAllExecutionEventRequest request = new FindAllExecutionEventRequest();
        request.setTurnId(turnId);
        return executionEventView.findAll(request);
    }

    /**
     * 判断执行事件是否适合沉淀为记忆证据。
     * <p>条件：包含至少一个原子命令完成事件，且没有失败事件。</p>
     *
     * @param events 执行事件列表
     * @return 是否适合沉淀
     */
    private boolean isSuitableForMemory(List<ExecutionEvent> events) {

        // 检查是否包含原子命令完成事件
        boolean hasCommandComplete = false;
        boolean hasFailure = false;

        // 遍历事件列表判断条件
        for (ExecutionEvent event : events) {
            if ("ATOMIC_COMMAND_COMPLETE".equals(event.getEventType())) {
                hasCommandComplete = true;
            }
            if (event.getFailureReason() != null && !event.getFailureReason().isBlank()) {
                hasFailure = true;
            }
        }

        // 至少有一个命令完成且没有失败事件
        return hasCommandComplete && !hasFailure;
    }

    /**
     * 构建记忆证据内容。
     * <p>将原子命令调用链和用户意图组装为结构化 JSON。</p>
     *
     * @param events         执行事件列表
     * @param commandContent 用户命令内容
     * @return 证据内容 JSON
     */
    private String buildEvidenceContent(List<ExecutionEvent> events, String commandContent) {
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("userIntent", commandContent);

        // 收集原子命令调用链
        List<Map<String, Object>> commandChain = new ArrayList<>();

        // 遍历事件列表提取原子命令调用
        for (ExecutionEvent event : events) {
            if ("ATOMIC_COMMAND_COMPLETE".equals(event.getEventType())) {
                Map<String, Object> commandStep = new HashMap<>();
                commandStep.put("commandName", event.getCommandName());
                commandStep.put("responseContent", event.getResponseContent());
                commandStep.put("sequenceNo", event.getSequenceNo());
                commandChain.add(commandStep);
            }
        }
        evidence.put("commandChain", commandChain);

        // 收集模型信息
        for (ExecutionEvent event : events) {
            if (event.getProviderName() != null && !event.getProviderName().isBlank()) {
                evidence.put("providerName", event.getProviderName());
                evidence.put("modelCode", event.getModelCode());
                break;
            }
        }

        return JsonUtils.toJsonStr(evidence);
    }

    /**
     * 创建记忆证据实体。
     *
     * @param turnId          轮次主键
     * @param taskId          任务主键
     * @param evidenceContent 证据内容
     * @return 记忆证据实体
     */
    private MemoryEvidence createEvidence(String turnId, String taskId, String evidenceContent) {
        MemoryEvidence evidence = new MemoryEvidence();
        evidence.setTurnId(turnId);
        evidence.setMemoryVersionId("");
        evidence.setEvidenceType("EXECUTION_TRACE");
        evidence.setEvidenceContent(evidenceContent);
        evidence.setQualityScore(0.8);
        evidence.setStatus(Status.ON);
        return evidence;
    }
}
