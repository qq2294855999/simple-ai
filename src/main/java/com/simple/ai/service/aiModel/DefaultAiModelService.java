package com.simple.ai.service.aiModel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.dto.agentDefinition.FindAllAgentDefinitionRequest;
import com.simple.ai.common.dto.aiModel.AiModelResponse;
import com.simple.ai.common.dto.aiModel.AiModelSaveRequest;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.aiModel.AiModel;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.service.aiModel.AiModelService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.aiModel.AiModelView;
import com.simple.ai.common.view.aiModelProvider.AiModelProviderView;
import com.simple.ai.view.agentChatMessage.AgentChatMessageRepository;
import com.simple.ai.view.task.TaskRepository;
import com.simple.ai.view.taskDetail.TaskDetailRepository;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 模型管理服务默认实现。
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAiModelService implements AiModelService {

    @Autowired
    private AiModelView aiModelView;

    @Autowired
    private AiModelProviderView aiModelProviderView;

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDetailRepository taskDetailRepository;

    @Autowired
    private AgentChatMessageRepository agentChatMessageRepository;

    @Override
    public List<AiModelResponse> findAll() {
        List<AiModel> models = aiModelView.findAll();
        return buildResponses(models);
    }

    @Override
    public List<AiModelResponse> findAvailableByAgentId(String agentId) {
        AssertUtils.notEmpty(agentId, "智能体主键不能为空");
        AgentDefinition agent = agentDefinitionView.findById(agentId);
        AssertUtils.notEmpty(agent, "智能体[{}]不存在", agentId);
        List<AiModel> models = aiModelView.findAll();
        return buildAvailableResponses(models);
    }

    @Override
    public String save(AiModelSaveRequest request) {
        AssertUtils.notEmpty(request, "模型保存请求不能为空");
        validateContextWindow(request);
        AiModelProvider provider = loadEnabledProvider(request.getProviderId());
        validateModelCodeUnique(request);

        // 创建与编辑按主键分流处理
        if (request.getId() == null || request.getId().isBlank()) {
            return createModel(request, provider);
        }
        return updateModel(request, provider);
    }

    @Override
    public void deleteById(String id) {
        AssertUtils.notEmpty(id, "模型主键不能为空");
        AiModel model = loadModel(id);
        assertModelNotReferenced(model.getId());
        aiModelView.deleteById(model.getId());
    }

    /**
     * 创建模型配置。
     *
     * @param request 保存请求
     * @param provider 启用供应商
     * @return 模型主键
     */
    private String createModel(AiModelSaveRequest request, AiModelProvider provider) {
        AiModel model = buildModel(request, provider);
        clearOtherSystemDefault(model);
        aiModelView.save(model);
        return model.getId();
    }

    /**
     * 更新模型配置。
     *
     * @param request 保存请求
     * @param provider 启用供应商
     * @return 模型主键
     */
    private String updateModel(AiModelSaveRequest request, AiModelProvider provider) {
        AiModel model = loadModel(request.getId());
        fillModel(model, request, provider);
        clearOtherSystemDefault(model);
        aiModelView.updateById(model);
        return model.getId();
    }

    /**
     * 校验上下文窗口。
     *
     * @param request 保存请求
     */
    private void validateContextWindow(AiModelSaveRequest request) {
        Integer contextWindow = request.getContextWindow();
        if (contextWindow == null) {
            return;
        }
        AssertUtils.isTrue(contextWindow > 0, "上下文窗口必须大于0");
    }

    /**
     * 加载启用供应商。
     *
     * @param providerId 供应商主键
     * @return 启用供应商
     */
    private AiModelProvider loadEnabledProvider(String providerId) {
        AiModelProvider provider = aiModelProviderView.findById(providerId);
        AssertUtils.notEmpty(provider, "供应商[{}]不存在", providerId);
        AssertUtils.isTrue(Status.ON.equals(provider.getStatus()), "供应商[{}]未启用", provider.getProviderName());
        return provider;
    }

    /**
     * 校验供应商内模型编码唯一性。
     *
     * @param request 保存请求
     */
    private void validateModelCodeUnique(AiModelSaveRequest request) {
        AiModel existsModel = aiModelView.findByProviderIdAndModelCode(request.getProviderId(), request.getModelCode());
        if (existsModel == null) {
            return;
        }
        AssertUtils.isTrue(existsModel.getId().equals(request.getId()), "供应商内模型编码[{}]已存在", request.getModelCode());
    }

    /**
     * 加载模型。
     *
     * @param id 模型主键
     * @return 模型配置
     */
    private AiModel loadModel(String id) {
        AiModel model = aiModelView.findById(id);
        AssertUtils.notEmpty(model, "模型[{}]不存在", id);
        return model;
    }

    /**
     * 构建模型实体。
     *
     * @param request 保存请求
     * @param provider 供应商实体
     * @return 模型实体
     */
    private AiModel buildModel(AiModelSaveRequest request, AiModelProvider provider) {
        AiModel model = new AiModel();
        fillModel(model, request, provider);
        return model;
    }

    /**
     * 回填模型可编辑字段。
     *
     * @param model 模型实体
     * @param request 保存请求
     * @param provider 供应商实体
     */
    private void fillModel(AiModel model, AiModelSaveRequest request, AiModelProvider provider) {
        model.setProviderId(provider.getId());
        model.setModelCode(request.getModelCode());
        model.setModelName(request.getModelName());
        model.setCapabilityConfig(request.getCapabilityConfig());
        model.setContextWindow(request.getContextWindow());
        model.setProviderDefault(Boolean.TRUE.equals(request.getProviderDefault()) ? 1 : 0);
        model.setSystemDefault(Boolean.TRUE.equals(request.getSystemDefault()) ? 1 : 0);
        model.setStatus(resolveStatus(request.getStatus()));
        model.setRemark(request.getRemark());
    }

    /**
     * 清理其他系统默认模型。
     *
     * @param model 当前待保存模型
     */
    private void clearOtherSystemDefault(AiModel model) {
        if (model.getSystemDefault() == null || model.getSystemDefault() != 1) {
            return;
        }
        AiModel currentDefault = aiModelView.findSystemDefault();
        if (currentDefault == null || currentDefault.getId().equals(model.getId())) {
            return;
        }
        currentDefault.setSystemDefault(0);
        aiModelView.updateById(currentDefault);
    }

    /**
     * 转换启停状态。
     *
     * @param status 状态数值
     * @return 框架状态枚举
     */
    private Status resolveStatus(Integer status) {
        if (status == null || status == Status.ON.getCode()) {
            return Status.ON;
        }
        AssertUtils.isTrue(status.equals(Status.OFF.getCode()), "状态值不合法");
        return Status.OFF;
    }

    /**
     * 校验模型没有历史或配置引用。
     *
     * @param modelId 模型主键
     */
    private void assertModelNotReferenced(String modelId) {
        assertAgentNotReferenced(modelId);
        assertTaskNotReferenced(modelId);
        assertTaskDetailNotReferenced(modelId);
        assertChatMessageNotReferenced(modelId);
    }

    /**
     * 校验智能体默认模型引用。
     *
     * @param modelId 模型主键
     */
    private void assertAgentNotReferenced(String modelId) {
        FindAllAgentDefinitionRequest request = new FindAllAgentDefinitionRequest();
        request.setDefaultModelId(modelId);
        List<AgentDefinition> agents = agentDefinitionView.findAll(request);
        AssertUtils.isTrue(agents.isEmpty(), "模型已被智能体默认配置引用，请先解除引用后再删除");
    }

    /**
     * 校验任务审计引用。
     *
     * @param modelId 模型主键
     */
    private void assertTaskNotReferenced(String modelId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getModelId, modelId);
        AssertUtils.isTrue(taskRepository.selectCount(wrapper) == 0, "模型已有任务审计记录，请停用而非删除");
    }

    /**
     * 校验任务详情审计引用。
     *
     * @param modelId 模型主键
     */
    private void assertTaskDetailNotReferenced(String modelId) {
        LambdaQueryWrapper<TaskDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDetail::getModelId, modelId);
        AssertUtils.isTrue(taskDetailRepository.selectCount(wrapper) == 0, "模型已有任务详情审计记录，请停用而非删除");
    }

    /**
     * 校验聊天消息审计引用。
     *
     * @param modelId 模型主键
     */
    private void assertChatMessageNotReferenced(String modelId) {
        LambdaQueryWrapper<AgentChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentChatMessage::getModelId, modelId);
        AssertUtils.isTrue(agentChatMessageRepository.selectCount(wrapper) == 0, "模型已有聊天审计记录，请停用而非删除");
    }

    /**
     * 组装模型管理响应。
     *
     * @param models 模型实体列表
     * @return 模型响应列表
     */
    private List<AiModelResponse> buildResponses(List<AiModel> models) {
        Map<String, AiModelProvider> providerMap = buildProviderMap();
        List<AiModelResponse> responses = new ArrayList<>();

        // 使用批量加载的供应商索引组装模型响应，避免列表查询产生 N+1
        for (AiModel model : models) {
            AiModelProvider provider = providerMap.get(model.getProviderId());
            responses.add(buildResponse(model, provider));
        }
        return responses;
    }

    /**
     * 组装可用模型响应。
     *
     * @param models 模型实体列表
     * @return 可用模型响应列表
     */
    private List<AiModelResponse> buildAvailableResponses(List<AiModel> models) {
        Map<String, AiModelProvider> providerMap = buildProviderMap();
        List<AiModelResponse> responses = new ArrayList<>();

        // 过滤模型与供应商均启用的可选项
        for (AiModel model : models) {
            AiModelProvider provider = providerMap.get(model.getProviderId());
            if (isAvailable(model, provider)) {
                responses.add(buildResponse(model, provider));
            }
        }
        return responses;
    }

    /**
     * 一次加载供应商并按主键建立局部索引。
     *
     * @return 供应商主键索引
     */
    private Map<String, AiModelProvider> buildProviderMap() {
        List<AiModelProvider> providers = aiModelProviderView.findAll();
        Map<String, AiModelProvider> providerMap = new HashMap<>();

        // 遍历供应商列表建立模型响应组装索引
        for (AiModelProvider provider : providers) {
            providerMap.put(provider.getId(), provider);
        }
        return providerMap;
    }

    /**
     * 判断模型是否可供运行时选择。
     *
     * @param model 模型实体
     * @param provider 供应商实体
     * @return 是否可用
     */
    private boolean isAvailable(AiModel model, AiModelProvider provider) {
        if (!Status.ON.equals(model.getStatus())) {
            return false;
        }
        return provider != null && Status.ON.equals(provider.getStatus());
    }

    /**
     * 组装模型响应。
     *
     * @param model 模型实体
     * @param provider 供应商实体
     * @return 模型响应
     */
    private AiModelResponse buildResponse(AiModel model, AiModelProvider provider) {
        AiModelResponse response = new AiModelResponse();
        response.setId(model.getId());
        response.setProviderId(model.getProviderId());
        response.setProviderName(provider == null ? "" : provider.getProviderName());
        response.setProtocolType(provider == null ? "" : provider.getProtocolType());
        response.setModelCode(model.getModelCode());
        response.setModelName(model.getModelName());
        response.setCapabilityConfig(model.getCapabilityConfig());
        response.setContextWindow(model.getContextWindow());
        response.setProviderDefault(model.getProviderDefault() != null && model.getProviderDefault() == 1);
        response.setSystemDefault(model.getSystemDefault() != null && model.getSystemDefault() == 1);
        response.setStatus(model.getStatus().getCode());
        response.setRemark(model.getRemark());
        response.setUpdateTime(model.getUpdateTime());
        return response;
    }
}
