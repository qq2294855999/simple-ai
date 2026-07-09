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
import com.simple.ai.common.view.agentMemoryDetail.AgentMemoryDetailView;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.InfoAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.UpdateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;
import com.simple.ai.common.copy.agentMemoryDetail.AgentMemoryDetailCopyMapper;

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
    private AgentMemoryDetailCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentMemoryDetailResponse> findAll(PageAgentMemoryDetailRequest pageRequest) {
        var pageInfo = agentMemoryDetailView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentMemoryDetailResponse findById(String id) {
        var agentMemoryDetail = agentMemoryDetailView.findById(id);
        AssertUtils.notEmpty(agentMemoryDetail, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentMemoryDetail);
    }

    @Override
    public String save(CreateAgentMemoryDetailRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        agentMemoryDetailView.save(entity);
        return entity.getId();
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
}

