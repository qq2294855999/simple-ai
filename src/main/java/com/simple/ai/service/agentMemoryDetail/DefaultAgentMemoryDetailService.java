package com.simple.ai.service.agentMemoryDetail;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.agentMemoryDetail.AgentMemoryDetailService;
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.agentMemoryDetail.AgentMemoryDetailView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.InfoAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.UpdateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailResponse;
import com.simple.ai.common.copy.agentMemoryDetail.AgentMemoryDetailCopyMapper;
import com.simple.common.mp.common.enums.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体记忆详情(agent_memory_detail)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentMemoryDetailService implements AgentMemoryDetailService {

    @Autowired
    private AgentMemoryDetailView agentMemoryDetailView;

    @Autowired
    private AgentMemoryView agentMemoryView;

    @Autowired
    private AgentMemoryDetailCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentMemoryDetailResponse> findAll(PageAgentMemoryDetailRequest pageRequest) {
        var pageInfo = agentMemoryDetailView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateAgentMemoryDetailResponse> findAggregateAll(PageAggregateAgentMemoryDetailRequest pageRequest) {

        // 查询记忆详情聚合分页数据
        return agentMemoryDetailView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAgentMemoryDetailResponse findById(String id) {
        var agentMemoryDetail = agentMemoryDetailView.findById(id);
        AssertUtils.notEmpty(agentMemoryDetail, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentMemoryDetail);
    }

    @Override
    public String save(CreateAgentMemoryDetailRequest createRequest) {

        // 校验记忆存在
        AgentMemory memory = agentMemoryView.findById(createRequest.getAgentMemoryId());
        AssertUtils.notEmpty(memory, "记忆[{}]不存在", createRequest.getAgentMemoryId());

        // 校验父步骤属于同一记忆
        validateStepBelongsToSameMemory(createRequest.getParentStepId(), createRequest.getAgentMemoryId(), "parentStepId");

        // 校验后继步骤属于同一记忆
        validateStepBelongsToSameMemory(createRequest.getNextStepId(), createRequest.getAgentMemoryId(), "nextStepId");

        // 构建并保存记忆详情
        var entity = copy.toEntity(createRequest);
        entity.setStatus(Status.ON);
        agentMemoryDetailView.save(entity);
        return entity.getId();
    }

    @Override
    public void saves(List<CreateAgentMemoryDetailRequest> createRequests) {
        List<AgentMemoryDetail> entities = new ArrayList<>();

        // 遍历创建请求列表，转换为批量写入实体
        for (CreateAgentMemoryDetailRequest createRequest : createRequests) {
            AgentMemoryDetail entity = copy.toEntity(createRequest);
            entities.add(entity);
        }
        agentMemoryDetailView.saves(entities);
    }

    @Override
    public String updateById(UpdateAgentMemoryDetailRequest updateRequest) {
        var agentMemoryDetail = agentMemoryDetailView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentMemoryDetail, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        agentMemoryDetailView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentMemoryDetailView.deleteByIds(ids);
    }

    /**
     * 校验步骤属于同一记忆，若不指定步骤ID则跳过。
     *
     * @param stepId 步骤主键
     * @param agentMemoryId 记忆主键
     * @param fieldName 字段名称用于错误提示
     */
    private void validateStepBelongsToSameMemory(String stepId, String agentMemoryId, String fieldName) {

        // 未指定步骤ID时跳过校验
        if (stepId == null || stepId.isEmpty()) {
            return;
        }

        // 查询步骤并校验归属
        AgentMemoryDetail step = agentMemoryDetailView.findById(stepId);
        AssertUtils.notEmpty(step, "[{}]对应的步骤[{}]不存在", fieldName, stepId);
        AssertUtils.isTrue(step.getAgentMemoryId().equals(agentMemoryId),
                "[{}]对应的步骤不属于同一记忆，期望记忆[{}]，实际记忆[{}]", fieldName, agentMemoryId, step.getAgentMemoryId());
    }
}

