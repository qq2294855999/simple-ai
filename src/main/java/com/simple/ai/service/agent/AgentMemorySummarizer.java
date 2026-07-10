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
 * Agent memory summarizer.
 *
 * @author qty
 */
@Component
public class AgentMemorySummarizer {

    @Autowired
    private TaskDetailView taskDetailView;

    @Autowired
    private AgentMemoryService agentMemoryService;

    @Autowired
    private AgentMemoryDetailService agentMemoryDetailService;

    /**
     * Summarize task details to agent memory.
     *
     * @param agentId agent id
     * @param taskId task id
     * @param memoryName memory name
     * @return agent memory id
     */
    public String summarize(String agentId, String taskId, String memoryName) {

        // Validate agent id
        AssertUtils.notEmpty(agentId, "agent id cannot be empty");

        // Validate task id
        AssertUtils.notEmpty(taskId, "task id cannot be empty");

        // Load task details
        List<TaskDetail> taskDetails = loadTaskDetails(taskId);

        // Save memory root
        String memoryId = saveMemory(agentId, memoryName, taskDetails);

        // Save memory detail chain
        saveMemoryDetails(memoryId, taskDetails);

        return memoryId;
    }

    /**
     * Load task details.
     *
     * @param taskId task id
     * @return task details
     */
    private List<TaskDetail> loadTaskDetails(String taskId) {
        FindAllTaskDetailRequest request = new FindAllTaskDetailRequest();
        request.setTaskId(taskId);
        return taskDetailView.findAll(request);
    }

    /**
     * Save memory root.
     *
     * @param agentId agent id
     * @param memoryName memory name
     * @param taskDetails task details
     * @return memory id
     */
    private String saveMemory(String agentId, String memoryName, List<TaskDetail> taskDetails) {
        CreateAgentMemoryRequest request = new CreateAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setMemoryName(memoryName);
        request.setStepName(findFirstStepName(taskDetails));
        request.setTriggerCondition(memoryName);
        request.setTriggerAction(findFirstReturnParams(taskDetails));
        request.setRemark("auto memory from task");
        return agentMemoryService.save(request);
    }

    /**
     * Save memory details.
     *
     * @param memoryId memory id
     * @param taskDetails task details
     */
    private void saveMemoryDetails(String memoryId, List<TaskDetail> taskDetails) {
        List<CreateAgentMemoryDetailRequest> requests = buildMemoryDetailRequests(memoryId, taskDetails);

        // Batch save memory details
        agentMemoryDetailService.saves(requests);
    }

    /**
     * Build memory detail requests.
     *
     * @param memoryId memory id
     * @param taskDetails task details
     * @return detail requests
     */
    private List<CreateAgentMemoryDetailRequest> buildMemoryDetailRequests(String memoryId, List<TaskDetail> taskDetails) {
        List<CreateAgentMemoryDetailRequest> requests = new ArrayList<>();

        // Convert task details to memory detail requests
        for (TaskDetail taskDetail : taskDetails) {
            CreateAgentMemoryDetailRequest request = buildMemoryDetailRequest(memoryId, taskDetail);
            requests.add(request);
        }
        return requests;
    }

    /**
     * Build memory detail request.
     *
     * @param memoryId memory id
     * @param taskDetail task detail
     * @return memory detail request
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
        request.setRemark("auto memory detail from task detail");
        return request;
    }

    /**
     * Find first step name.
     *
     * @param taskDetails task details
     * @return first step name
     */
    private String findFirstStepName(List<TaskDetail> taskDetails) {

        // Use default step name when task detail is empty
        if (taskDetails.isEmpty()) {
            return "command execution";
        }
        TaskDetail first = taskDetails.get(0);
        return first.getTaskName();
    }

    /**
     * Find first return params.
     *
     * @param taskDetails task details
     * @return return params
     */
    private String findFirstReturnParams(List<TaskDetail> taskDetails) {

        // Use empty result when task detail is empty
        if (taskDetails.isEmpty()) {
            return "";
        }
        TaskDetail first = taskDetails.get(0);
        return first.getReturnParams();
    }
}
