package com.simple.ai.view.agentRule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentRule.DeleteAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindOneAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体规则(agent_rule)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentRuleView implements AgentRuleView {

    @Autowired
    private AgentRuleRepository repository;

    @Override
    public IPage<AgentRule> findAll(PageAgentRuleRequest pageRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getAgentId()), AgentRule::getAgentId, pageRequest.getAgentId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, pageRequest.getDefinitionDesc())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTriggerCondition()), AgentRule::getTriggerCondition, pageRequest.getTriggerCondition())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTriggerAction()), AgentRule::getTriggerAction, pageRequest.getTriggerAction())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentRule::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AgentRule::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentRule::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentRule.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateAgentRuleResponse> findAggregateAll(PageAggregateAgentRuleRequest pageRequest) {

        // 构建分页边界
        Page<AgentRule> page = pageRequest.getPage(AgentRule.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合分页记录和总数
        List<PageAggregateAgentRuleResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateAgentRuleResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AgentRule> findAll(FindAllAgentRuleRequest findAllRequest, FindAllAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = buildFindAllWrapper(findAllRequest, neRequest);
        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentRule findOne(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        List<AgentRule> list = repository.selectList(queryWrapper);

        // 空结果直接返回空对象引用
        if (list.isEmpty()) {
            return null;
        }

        // 校验条件单查结果唯一性
        if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AgentRule findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentRule agentRule) {
        repository.insert(agentRule);
    }

    @Override
    public void updateById(AgentRule agentRule) {
        repository.updateById(agentRule);
    }

    @Override
    public void updateById(List<AgentRule> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentRule> list) {

        // 空集合不执行批量新增
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentRuleRequest request) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentRule::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentId()), AgentRule::getAgentId, request.getAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getDefinitionDesc()), AgentRule::getDefinitionDesc, request.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(request.getTriggerCondition()), AgentRule::getTriggerCondition, request.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(request.getTriggerAction()), AgentRule::getTriggerAction, request.getTriggerAction())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentRule::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), AgentRule::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentRule::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }

    /**
     * 构建列表查询条件。
     *
     * @param findAllRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentRule> buildFindAllWrapper(FindAllAgentRuleRequest findAllRequest, FindAllAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentRule::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentId()), AgentRule::getAgentId, findAllRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, findAllRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTriggerCondition()), AgentRule::getTriggerCondition, findAllRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTriggerAction()), AgentRule::getTriggerAction, findAllRequest.getTriggerAction())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentRule::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AgentRule::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentRule::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentRule::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentRule::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentRule::getTriggerCondition, neRequest.getTriggerCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerAction()), AgentRule::getTriggerAction, neRequest.getTriggerAction())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentRule::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentRule::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentRule::getRemark, neRequest.getRemark());
        return queryWrapper;
    }

    /**
     * 构建单条查询条件。
     *
     * @param findOneRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentRule> buildFindOneWrapper(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentRule::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentRule::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTriggerCondition()), AgentRule::getTriggerCondition, findOneRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTriggerAction()), AgentRule::getTriggerAction, findOneRequest.getTriggerAction())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentRule::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentRule::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentRule::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentRule::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentRule::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentRule::getTriggerCondition, neRequest.getTriggerCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerAction()), AgentRule::getTriggerAction, neRequest.getTriggerAction())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentRule::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentRule::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentRule::getRemark, neRequest.getRemark());
        return queryWrapper;
    }
}


