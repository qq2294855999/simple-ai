package com.simple.ai.service.agent.tool;

import com.simple.ai.common.dto.agentMemory.*;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.service.memory.MemoryDistiller;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 记忆能力 AI 工具回调组件。
 * <p>为 AI 模型提供 4 个 ToolCallback：
 * queryMemory、getMemorySteps、createMemory、reviseMemory。
 * 所有工具均从 ToolContext 取 userId/agentId 等运行时上下文，
 * 调用方不得通过 AI 输入参数传入这些受控字段。</p>
 *
 * @author qty
 */
@Component
public class MemoryToolCallback {

    /**
     * 智能体记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 智能体记忆步骤视图
     */
    @Autowired
    private AgentMemoryStepView agentMemoryStepView;

    /**
     * 记忆蒸馏器
     */
    @Autowired
    private MemoryDistiller memoryDistiller;

    // ──────────────────────────── queryMemory ────────────────────────────

    /**
     * 查询匹配的记忆工具。
     * <p>按 agentId 限定范围，按 keyword 模糊匹配 memoryName，
     * 返回简化列表（不含步骤序列），供 AI 判断是否有可复用的记忆。</p>
     *
     * @return 工具回调
     */
    public ToolCallback queryMemory() {
        return FunctionToolCallback.builder("queryMemory", (QueryMemoryToolRequest req, ToolContext toolContext) -> {
                                       // 从 ToolContext 获取运行时上下文中的 agentId，限定查询范围
                                       Map<String, Object> context = toolContext != null ? toolContext.getContext() : null;
                                       String agentId = null;
                                       if (context != null) {
                                           agentId = (String) context.get("agentId");
                                       }
                                       AssertUtils.notEmpty(agentId, "当前智能体身份为空");

                                       // 查询当前智能体下所有启用状态的记忆
                                       FindAllAgentMemoryRequest findAll = new FindAllAgentMemoryRequest();
                                       findAll.setAgentId(agentId);
                                       findAll.setStatus(Status.ON);
                                       List<AgentMemory> all = agentMemoryView.findAll(findAll);

                                       // 按 keyword 模糊匹配 memoryName
                                       String keyword = req != null ? req.getKeyword() : null;
                                       List<Map<String, Object>> result = new ArrayList<>();
                                       for (AgentMemory memory : all) {
                                           if (keyword == null || keyword.isBlank() || (memory.getMemoryName() != null && memory.getMemoryName().contains(keyword))) {

                                               Map<String, Object> item = new LinkedHashMap<>();
                                               item.put("memoryId", memory.getId());
                                               item.put("memoryName", memory.getMemoryName());
                                               item.put("summary", memory.getSummary());
                                               item.put("paramsDefinition", memory.getParamsDefinition());
                                               result.add(item);
                                           }
                                       }
                                       return result;
                                   })
                                   .description("查询匹配的记忆。参数：keyword（搜索关键词，可选，为空时返回全部启用记忆）。" + "返回记忆列表，每项含 memoryId、memoryName、summary、paramsDefinition。"
                                                + "查询范围由当前会话的智能体上下文自动限定。")
                                   .inputType(QueryMemoryToolRequest.class)
                                   .build();
    }

    // ──────────────────────────── getMemorySteps ────────────────────────────

    /**
     * 获取记忆步骤工具。
     * <p>按 memoryId 查询完整的步骤序列，供 AI 分析记忆的执行流程。</p>
     *
     * @return 工具回调
     */
    public ToolCallback getMemorySteps() {
        return FunctionToolCallback.builder("getMemorySteps", (GetMemoryStepsToolRequest req, ToolContext toolContext) -> {
                                       String memoryId = req != null ? req.getMemoryId() : null;
                                       AssertUtils.notEmpty(memoryId, "记忆ID不能为空");

                                       // 查询该记忆的所有步骤（按 sequenceNo 排序）
                                       List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(memoryId);

                                       // 构造返回结果：不暴露数据库 ID，仅返回 AI 需要的字段
                                       List<Map<String, Object>> result = new ArrayList<>();
                                       for (AgentMemoryStep step : steps) {
                                           Map<String, Object> item = new LinkedHashMap<>();
                                           item.put("sequenceNo", step.getSequenceNo());
                                           item.put("stepName", step.getStepName());
                                           item.put("atomicCommandCode", step.getAtomicCommandCode());
                                           item.put("argsTemplate", step.getArgsTemplate());
                                           result.add(item);
                                       }
                                       return result;
                                   })
                                   .description("获取某记忆的步骤序列。参数：memoryId（记忆ID，必填，由 queryMemory 返回）。"
                                                + "返回步骤列表，每项含 sequenceNo、stepName、atomicCommandCode、argsTemplate。")
                                   .inputType(GetMemoryStepsToolRequest.class)
                                   .build();
    }

    // ──────────────────────────── createMemory ────────────────────────────

    /**
     * 蒸馏创建记忆工具（写操作，有副作用）。
     * <p>AI 探索成功后，调用蒸馏器从 task 执行轨迹提炼记忆和步骤序列。
     * distill 内部做幂等校验（同一 taskId 不会重复蒸馏）。</p>
     *
     * @return 工具回调
     */
    public ToolCallback createMemory() {
        return FunctionToolCallback.builder("createMemory", (CreateMemoryToolRequest req, ToolContext toolContext) -> {
                                       String taskId = req != null ? req.getTaskId() : null;
                                       AssertUtils.notEmpty(taskId, "来源任务ID不能为空");

                                       // 调蒸馏器：内部做幂等校验 + 自动写 agentId/clientId
                                       memoryDistiller.distill(taskId);

                                       // 蒸馏完成后，按 sourceTaskId 回查等待创建的记忆
                                       FindAllAgentMemoryRequest findReq = new FindAllAgentMemoryRequest();
                                       findReq.setSourceTaskId(taskId);
                                       List<AgentMemory> memories = agentMemoryView.findAll(findReq);
                                       AssertUtils.notEmpty(memories, "蒸馏记忆失败，未找到对应记录");
                                       AgentMemory memory = memories.get(0);

                                       // 统计步骤数
                                       List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(memory.getId());

                                       // 构造返回值
                                       Map<String, Object> result = new LinkedHashMap<>();
                                       result.put("memoryId", memory.getId());
                                       result.put("memoryName", memory.getMemoryName());
                                       result.put("stepCount", steps != null ? steps.size() : 0);
                                       result.put("paramsDefinition", memory.getParamsDefinition());
                                       return result;
                                   })
                                   .description("从任务执行轨迹蒸馏记忆。参数：taskId（来源任务ID，必填）。" + "蒸馏器自动从 task 和 task_details 提炼最短执行链，识别参数占位符，创建记忆和步骤序列。"
                                                + "返回 {memoryId, memoryName, stepCount, paramsDefinition}。")
                                   .inputType(CreateMemoryToolRequest.class)
                                   .build();
    }

    // ──────────────────────────── reviseMemory ────────────────────────────

    /**
     * 覆盖修订记忆工具（写操作，有副作用）。
     * <p>重新探索成功后调用，删除原记忆的所有步骤，
     * 按传入命令序列重新构建步骤并覆盖记忆名称和参数定义。
     * distillRevision 内部是 @Transactional 的原子操作。</p>
     *
     * @return 工具回调
     */
    public ToolCallback reviseMemory() {
        return FunctionToolCallback.builder("reviseMemory", (ReviseMemoryToolRequest req, ToolContext toolContext) -> {
                                       String memoryId = req != null ? req.getMemoryId() : null;
                                       AssertUtils.notEmpty(memoryId, "被修订的记忆ID不能为空");

                                       List<CommandStep> steps = req != null ? req.getSteps() : null;
                                       AssertUtils.notEmpty(steps, "命令序列不能为空");

                                       String paramsDefinition = req != null ? req.getParamsDefinition() : null;
                                       AssertUtils.notEmpty(paramsDefinition, "参数定义不能为空");

                                       // 调蒸馏器的覆盖修订方法，内部删旧步骤 + 插新步骤 + 覆盖记忆
                                       String memoryNameHint = req != null ? req.getMemoryNameHint() : null;
                                       memoryDistiller.distillRevision(memoryId, steps, paramsDefinition, memoryNameHint);

                                       // 返回成功结果
                                       Map<String, Object> result = new LinkedHashMap<>();
                                       result.put("memoryId", memoryId);
                                       result.put("success", true);
                                       return result;
                                   })
                                   .description("覆盖修订已有记忆。参数：memoryId（被修订的记忆ID，必填）、"
                                                + "steps（新命令序列，必填，每项含 atomicCommandCode/stepName/argsTemplate/timeoutMs/failureStrategy）、"
                                                + "paramsDefinition（参数定义JSON，必填）、memoryNameHint（记忆名称提示，可选，为空则保留原名）。"
                                                + "内部删旧步骤 + 插新步骤 + 覆盖记忆信息，是原子事务操作。")
                                   .inputType(ReviseMemoryToolRequest.class)
                                   .build();
    }
}
