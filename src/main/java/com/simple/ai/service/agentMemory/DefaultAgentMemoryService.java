package com.simple.ai.service.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentMemory.AgentMemoryCopyMapper;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.UpdateAgentMemoryRequest;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.view.agentMemory.AgentMemoryRepository;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体记忆(agent_memory)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentMemoryService implements AgentMemoryService {

    @Autowired
    private AgentMemoryView agentMemoryView;

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AgentMemoryRepository agentMemoryRepository;

    @Autowired
    private AgentMemoryCopyMapper copy;

    @Override
    public IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest) {

        // 查询单表分页并转换响应
        var pageInfo = agentMemoryView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateAgentMemoryResponse> findAggregateAll(PageAggregateAgentMemoryRequest pageRequest) {

        // 查询记忆聚合分页数据
        return agentMemoryView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAgentMemoryResponse findById(String id) {

        // 查询并校验智能体记忆存在
        var agentMemory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(agentMemory, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentMemory);
    }

    @Override
    public String save(CreateAgentMemoryRequest createRequest) {

        // 校验智能体存在
        validateAgentExists(createRequest.getAgentId());

        // 校验同智能体记忆名称唯一
        checkMemoryNameUnique(createRequest.getAgentId(), createRequest.getMemoryName(), null);

        // 构建并保存智能体记忆
        AgentMemory entity = copy.toEntity(createRequest);
        entity.setStatus(Status.ON);
        agentMemoryView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentMemoryRequest updateRequest) {

        // 查询并校验智能体记忆存在
        var agentMemory = agentMemoryView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentMemory, "主键[{}]的数据不存在", updateRequest.getId());

        // 构建并更新智能体记忆
        var entity = copy.toEntity(updateRequest);
        agentMemoryView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 校验所有记忆存在
        validateMemoriesExist(ids);

        // 按依赖顺序执行级联清理
        executeCascadeDelete(ids);
    }

    /**
     * 校验所有记忆存在。
     *
     * @param ids 记忆主键列表
     */
    private void validateMemoriesExist(List<String> ids) {

        // 批量查询记忆数据
        List<AgentMemory> memories = agentMemoryRepository.selectBatchIds(ids);

        // 校验数量一致，确保所有主键有效
        AssertUtils.isTrue(memories.size() == ids.size(), "存在无效的记忆主键，无法删除");
    }

    /**
     * 执行记忆级联删除。
     * 清理顺序：task_detail → task → agent_memory_detail → agent_memory。
     *
     * @param ids 记忆主键列表
     */
    private void executeCascadeDelete(List<String> ids) {

        // 先清理任务详情，再清理任务主表
        agentMemoryRepository.deleteTaskDetailsByMemoryIds(ids);
        agentMemoryRepository.deleteTasksByMemoryIds(ids);

        // 清理记忆详情
        agentMemoryRepository.deleteMemoryDetailsByMemoryIds(ids);

        // 删除记忆主表
        agentMemoryView.deleteByIds(ids);
    }

    /**
     * 校验智能体存在。
     *
     * @param agentId 智能体主键
     */
    private void validateAgentExists(String agentId) {
        FindOneAgentDefinitionRequest request = new FindOneAgentDefinitionRequest();
        request.setId(agentId);

        // 查询并校验智能体存在
        AgentDefinition agentDefinition = agentDefinitionView.findOne(request);
        AssertUtils.notEmpty(agentDefinition, "智能体[{}]不存在", agentId);
    }

    /**
     * 校验同智能体记忆名称唯一。
     *
     * @param agentId 智能体主键
     * @param memoryName 记忆名称
     * @param excludeId 排除主键
     */
    private void checkMemoryNameUnique(String agentId, String memoryName, String excludeId) {
        FindOneAgentMemoryRequest request = new FindOneAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setMemoryName(memoryName);

        FindOneAgentMemoryRequest neRequest = new FindOneAgentMemoryRequest();
        neRequest.setId(excludeId);

        // 查询同智能体同名记忆数量
        Long count = agentMemoryView.findCount(request, neRequest);
        AssertUtils.isTrue(count == 0, "智能体下记忆名称[{}]已存在", memoryName);
    }
}
