package com.simple.ai.view.agentRule;

import java.util.Date;

import cn.hutool.core.collection.CollectionUtil;
import com.simple.common.core.utils.AssertUtils;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindOneAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.DeleteAgentRuleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

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
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getName()), AgentRule::getName, pageRequest.getName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, pageRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentRule::getStatus, pageRequest.getStatus());
        return repository.selectPage(pageRequest.getPage(AgentRule.class), queryWrapper);
    }

    @Override
    public List<AgentRule> findAll(FindAllAgentRuleRequest findAllRequest, FindAllAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentRule::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getName()), AgentRule::getName, findAllRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, findAllRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentRule::getStatus, findAllRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentRule::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentRule::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentRule::getStatus, neRequest.getStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentRule findOne(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentRule::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AgentRule::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentRule::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentRule::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentRule::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentRule::getStatus, neRequest.getStatus());

        List<AgentRule> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest) {
        LambdaQueryWrapper<AgentRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentRule::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AgentRule::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentRule::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentRule::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentRule::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentRule::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentRule::getStatus, neRequest.getStatus());
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
                    .eq(ObjUtil.isNotEmpty(request.getName()), AgentRule::getName, request.getName())
                    .eq(ObjUtil.isNotEmpty(request.getDefinitionDesc()), AgentRule::getDefinitionDesc, request.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentRule::getStatus, request.getStatus());
        repository.delete(queryWrapper);
    }
}

