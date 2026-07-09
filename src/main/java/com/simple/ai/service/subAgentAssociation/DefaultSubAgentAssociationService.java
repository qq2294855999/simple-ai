package com.simple.ai.service.subAgentAssociation;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.subAgentAssociation.SubAgentAssociationService;
import com.simple.ai.common.entity.subAgentAssociation.SubAgentAssociation;
import com.simple.ai.common.view.subAgentAssociation.SubAgentAssociationView;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.InfoSubAgentAssociationResponse;
import com.simple.ai.common.dto.subAgentAssociation.CreateSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.UpdateSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationRequest;
import com.simple.ai.common.copy.subAgentAssociation.SubAgentAssociationCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 子智能体关联(sub_agent_association)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultSubAgentAssociationService implements SubAgentAssociationService {

    @Autowired
    private SubAgentAssociationView subAgentAssociationView;

    @Autowired
    private SubAgentAssociationCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageSubAgentAssociationResponse> findAll(PageSubAgentAssociationRequest pageRequest) {
        var pageInfo = subAgentAssociationView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoSubAgentAssociationResponse findById(String id) {
        var subAgentAssociation = subAgentAssociationView.findById(id);
        AssertUtils.notEmpty(subAgentAssociation, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(subAgentAssociation);
    }

    @Override
    public String save(CreateSubAgentAssociationRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        subAgentAssociationView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateSubAgentAssociationRequest updateRequest) {
        var subAgentAssociation = subAgentAssociationView.findById(updateRequest.getId());
        AssertUtils.notEmpty(subAgentAssociation, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        subAgentAssociationView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        subAgentAssociationView.deleteByIds(ids);
    }
}

