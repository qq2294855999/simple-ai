package com.simple.ai.service.subAgentRelation;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.subAgentRelation.SubAgentRelationService;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;
import com.simple.ai.common.copy.subAgentRelation.SubAgentRelationCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 子智能体关联(sub_agent_relation)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultSubAgentRelationService implements SubAgentRelationService {

    @Autowired
    private SubAgentRelationView subAgentRelationView;

    @Autowired
    private SubAgentRelationCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageSubAgentRelationResponse> findAll(PageSubAgentRelationRequest pageRequest) {
        var pageInfo = subAgentRelationView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoSubAgentRelationResponse findById(String id) {
        var subAgentRelation = subAgentRelationView.findById(id);
        AssertUtils.notEmpty(subAgentRelation, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(subAgentRelation);
    }

    @Override
    public String save(CreateSubAgentRelationRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        subAgentRelationView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateSubAgentRelationRequest updateRequest) {
        var subAgentRelation = subAgentRelationView.findById(updateRequest.getId());
        AssertUtils.notEmpty(subAgentRelation, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        subAgentRelationView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        subAgentRelationView.deleteByIds(ids);
    }
}

