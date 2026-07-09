package com.simple.ai.service.agent;

import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.agentMemoryDetail.AgentMemoryDetailService;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体记忆摘要器。
 *
 * @author qty
 */
@Component
public class AgentMemorySummarizer {

    /**
     * 任务详情视图
     */
    @Autowired
    private TaskDetailView taskDetailView;

    /**
     * 智能体记忆服务
     */
    @Autowired
    private AgentMemoryService agentMemoryService;

    /**
     * 智能体记忆详情服务
     */
    @Autowired
    private AgentMemoryDetailService agentMemoryDetailService;

    /**
     * 按任务详情沉淀智能体记忆。
     *
     * @param agentId 智能体ID
     * @param taskId 任务ID
     * @param memoryName 记忆名称
     * @return 智能体记忆ID
     */
    public String summarize(String agentId, String taskId, String memoryName) {

        // 参数校验：智能体ID不能为空
        AssertUtils.notEmpty(agentId, "智能体ID不能为空");

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(taskId, "任务ID不能为空");

        // 查询任务详情链路
        List<TaskDetail> taskDetails = loadTaskDetails(taskId);

        // 创建智能体记忆主记录
        String memoryId = saveMemory(agentId, memoryName, taskDetails);

        // 创建智能体记忆详情链路
        saveMemoryDetails(memoryId, taskDetails);

        return memoryId;
    }

    /**
     * 查询任务详情链路。
     *
     * @param taskId 任务ID
     * @return 任务详情列表
     */
    private List<TaskDetail> loadTaskDetails(String taskId) {
        FindAllTaskDetailRequest request = new FindAllTaskDetailRequest();
        request.setTaskId(taskId);
        return taskDetailView.findAll(request);
    }

    /**
     * 创建智能体记忆主记录。
     *
     * @param agentId 智能体ID
     * @param memoryName 记忆名称
     * @param taskDetails 任务详情列表
     * @return 智能体记忆ID
     */
    private String saveMemory(String agentId, String memoryName, List<TaskDetail> taskDetails) {
        CreateAgentMemoryRequest request = new CreateAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setMemoryName(memoryName);
        request.setStepName(findFirstStepName(taskDetails));
        request.setTriggerCondition(memoryName);
        request.setTriggerAction(findFirstReturnParams(taskDetails));
        request.setReserver("");
        request.setRemark("智能体命令调度自动沉淀记忆");
        return agentMemoryService.save(request);
    }

    /**
     * 创建智能体记忆详情链路。
     *
     * @param memoryId 智能体记忆ID
     * @param taskDetails 任务详情列表
     */
    private void saveMemoryDetails(String memoryId, List<TaskDetail> taskDetails) {
        List<CreateAgentMemoryDetailRequest> requests = buildMemoryDetailRequests(memoryId, taskDetails);

        // 批量保存记忆详情，避免任务详情较多时产生逐条写入
        agentMemoryDetailService.saves(requests);
    }

    /**
     * 构建记忆详情创建请求列表。
     *
     * @param memoryId 智能体记忆ID
     * @param taskDetails 任务详情列表
     * @return 记忆详情创建请求列表
     */
    private List<CreateAgentMemoryDetailRequest> buildMemoryDetailRequests(String memoryId, List<TaskDetail> taskDetails) {
        List<CreateAgentMemoryDetailRequest> requests = new ArrayList<>();

        // 遍历任务详情链路，转换为记忆详情创建请求
        for (TaskDetail taskDetail : taskDetails) {
            CreateAgentMemoryDetailRequest request = buildMemoryDetailRequest(memoryId, taskDetail);
            requests.add(request);
        }
        return requests;
    }

    /**
     * 构建单条智能体记忆详情创建请求。
     *
     * @param memoryId 智能体记忆ID
     * @param taskDetail 任务详情
     * @return 记忆详情创建请求
     */
    private CreateAgentMemoryDetailRequest buildMemoryDetailRequest(String memoryId, TaskDetail taskDetail) {
        CreateAgentMemoryDetailRequest request = new CreateAgentMemoryDetailRequest();
        request.setAgentMemoryId(memoryId);
        request.setStepName(taskDetail.getTaskName());
        request.setStepType(taskDetail.getStepType());
        request.setExecContent(taskDetail.getRequestParams());
        request.setReturnDataFormat(taskDetail.getReturnParams());
        request.setParentStepId(taskDetail.getParentTaskId());
        request.setNextStepId(taskDetail.getNextTaskId());
        request.setBranchCondition(taskDetail.getBranchCondition());
        request.setBranchRoute(taskDetail.getBranchRoute());
        request.setModel("");
        request.setReserver("");
        request.setRemark("任务详情自动沉淀为记忆详情");
        return request;
    }

    /**
     * 查询首个步骤名称。
     *
     * @param taskDetails 任务详情列表
     * @return 首个步骤名称
     */
    private String findFirstStepName(List<TaskDetail> taskDetails) {

        // 任务详情为空时使用默认步骤名称
        if (taskDetails.isEmpty()) {
            return "命令执行";
        }
        TaskDetail first = taskDetails.get(0);
        return first.getTaskName();
    }

    /**
     * 查询首个返回参数。
     *
     * @param taskDetails 任务详情列表
     * @return 首个返回参数
     */
    private String findFirstReturnParams(List<TaskDetail> taskDetails) {

        // 任务详情为空时使用空结果
        if (taskDetails.isEmpty()) {
            return "";
        }
        TaskDetail first = taskDetails.get(0);
        return first.getReturnParams();
    }

}
