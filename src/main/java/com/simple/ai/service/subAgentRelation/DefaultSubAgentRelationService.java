package com.simple.ai.service.subAgentRelation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.subAgentRelation.SubAgentRelationCopyMapper;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.FindOneSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.service.subAgentRelation.SubAgentRelationService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private SubAgentRelationCopyMapper copy;

    @Override
    public IPage<PageSubAgentRelationResponse> findAll(PageSubAgentRelationRequest pageRequest) {
        var pageInfo = subAgentRelationView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateSubAgentRelationResponse> findAggregateAll(PageAggregateSubAgentRelationRequest pageRequest) {

        // 查询前端展示需要的聚合分页数据
        return subAgentRelationView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoSubAgentRelationResponse findById(String id) {
        var subAgentRelation = subAgentRelationView.findById(id);
        AssertUtils.notEmpty(subAgentRelation, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(subAgentRelation);
    }

    @Override
    public String save(CreateSubAgentRelationRequest createRequest) {

        // 校验主智能体和子智能体存在性
        validateAgentsExist(createRequest);

        // 校验不能自关联
        validateNotSelfRelation(createRequest);

        // 校验关系唯一性
        validateRelationUnique(createRequest);

        // 保存子智能体关系
        SubAgentRelation entity = copy.toEntity(createRequest);
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

    /**
     * 校验主智能体和子智能体存在性。
     *
     * @param createRequest 创建请求
     */
    private void validateAgentsExist(CreateSubAgentRelationRequest createRequest) {
        AgentDefinition mainAgent = findAgentById(createRequest.getMainAgentId());
        AssertUtils.notEmpty(mainAgent, "主智能体[{}]不存在", createRequest.getMainAgentId());
        AgentDefinition subAgent = findAgentById(createRequest.getSubAgentId());
        AssertUtils.notEmpty(subAgent, "子智能体[{}]不存在", createRequest.getSubAgentId());
    }

    /**
     * 根据主键查询智能体。
     *
     * @param agentId 智能体主键
     * @return 智能体定义
     */
    private AgentDefinition findAgentById(String agentId) {
        FindOneAgentDefinitionRequest request = new FindOneAgentDefinitionRequest();
        request.setId(agentId);
        return agentDefinitionView.findOne(request);
    }

    /**
     * 校验主子智能体不能相同。
     *
     * @param createRequest 创建请求
     */
    private void validateNotSelfRelation(CreateSubAgentRelationRequest createRequest) {
        boolean notSelfRelation = !createRequest.getMainAgentId().equals(createRequest.getSubAgentId());
        AssertUtils.isTrue(notSelfRelation, "主智能体和子智能体不能相同");
    }

    /**
     * 校验主子智能体关系唯一性。
     *
     * @param createRequest 创建请求
     */
    private void validateRelationUnique(CreateSubAgentRelationRequest createRequest) {
        FindOneSubAgentRelationRequest request = new FindOneSubAgentRelationRequest();
        request.setMainAgentId(createRequest.getMainAgentId());
        request.setSubAgentId(createRequest.getSubAgentId());
        SubAgentRelation existsRelation = subAgentRelationView.findOne(request);
        AssertUtils.isTrue(existsRelation == null, "主子智能体关系已存在");
    }
}

