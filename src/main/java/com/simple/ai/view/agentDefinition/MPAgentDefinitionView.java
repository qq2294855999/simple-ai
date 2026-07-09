package com.simple.ai.view.agentDefinition;

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
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.FindAllAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteAgentDefinitionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 智能体定义(agent_definition)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentDefinitionView implements AgentDefinitionView {

    @Autowired
    private AgentDefinitionRepository repository;

    @Override
    public IPage<AgentDefinition> findAll(PageAgentDefinitionRequest pageRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getName()), AgentDefinition::getName, pageRequest.getName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, pageRequest.getDefinitionDesc())
                    .like(ObjUtil.isNotEmpty(pageRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, pageRequest.getFirstPrinciple())
                    .like(ObjUtil.isNotEmpty(pageRequest.getSecondRule()), AgentDefinition::getSecondRule, pageRequest.getSecondRule())
                    .like(ObjUtil.isNotEmpty(pageRequest.getThirdSkill()), AgentDefinition::getThirdSkill, pageRequest.getThirdSkill())
                    .like(ObjUtil.isNotEmpty(pageRequest.getModel()), AgentDefinition::getModel, pageRequest.getModel())
                    .like(ObjUtil.isNotEmpty(pageRequest.getCreateBy()), AgentDefinition::getCreateBy, pageRequest.getCreateBy())
                    .like(ObjUtil.isNotEmpty(pageRequest.getUpdateBy()), AgentDefinition::getUpdateBy, pageRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentDefinition::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AgentDefinition::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentDefinition::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentDefinition.class), queryWrapper);
    }

    @Override
    public List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest, FindAllAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentDefinition::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getName()), AgentDefinition::getName, findAllRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, findAllRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, findAllRequest.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSecondRule()), AgentDefinition::getSecondRule, findAllRequest.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getThirdSkill()), AgentDefinition::getThirdSkill, findAllRequest.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getModel()), AgentDefinition::getModel, findAllRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCreateBy()), AgentDefinition::getCreateBy, findAllRequest.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getUpdateBy()), AgentDefinition::getUpdateBy, findAllRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentDefinition::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AgentDefinition::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentDefinition::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentDefinition::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentDefinition::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, neRequest.getFirstPrinciple())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSecondRule()), AgentDefinition::getSecondRule, neRequest.getSecondRule())
                    .ne(ObjUtil.isNotEmpty(neRequest.getThirdSkill()), AgentDefinition::getThirdSkill, neRequest.getThirdSkill())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentDefinition::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCreateBy()), AgentDefinition::getCreateBy, neRequest.getCreateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUpdateBy()), AgentDefinition::getUpdateBy, neRequest.getUpdateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentDefinition::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentDefinition::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentDefinition::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentDefinition::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AgentDefinition::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, findOneRequest.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSecondRule()), AgentDefinition::getSecondRule, findOneRequest.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getThirdSkill()), AgentDefinition::getThirdSkill, findOneRequest.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModel()), AgentDefinition::getModel, findOneRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCreateBy()), AgentDefinition::getCreateBy, findOneRequest.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getUpdateBy()), AgentDefinition::getUpdateBy, findOneRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentDefinition::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentDefinition::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentDefinition::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentDefinition::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentDefinition::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, neRequest.getFirstPrinciple())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSecondRule()), AgentDefinition::getSecondRule, neRequest.getSecondRule())
                    .ne(ObjUtil.isNotEmpty(neRequest.getThirdSkill()), AgentDefinition::getThirdSkill, neRequest.getThirdSkill())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentDefinition::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCreateBy()), AgentDefinition::getCreateBy, neRequest.getCreateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUpdateBy()), AgentDefinition::getUpdateBy, neRequest.getUpdateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentDefinition::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentDefinition::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentDefinition::getRemark, neRequest.getRemark());

        List<AgentDefinition> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentDefinition::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AgentDefinition::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, findOneRequest.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSecondRule()), AgentDefinition::getSecondRule, findOneRequest.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getThirdSkill()), AgentDefinition::getThirdSkill, findOneRequest.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModel()), AgentDefinition::getModel, findOneRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCreateBy()), AgentDefinition::getCreateBy, findOneRequest.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getUpdateBy()), AgentDefinition::getUpdateBy, findOneRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentDefinition::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentDefinition::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentDefinition::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentDefinition::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentDefinition::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, neRequest.getFirstPrinciple())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSecondRule()), AgentDefinition::getSecondRule, neRequest.getSecondRule())
                    .ne(ObjUtil.isNotEmpty(neRequest.getThirdSkill()), AgentDefinition::getThirdSkill, neRequest.getThirdSkill())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentDefinition::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCreateBy()), AgentDefinition::getCreateBy, neRequest.getCreateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUpdateBy()), AgentDefinition::getUpdateBy, neRequest.getUpdateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentDefinition::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentDefinition::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentDefinition::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AgentDefinition findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentDefinition agentDefinition) {
        repository.insert(agentDefinition);
    }

    @Override
    public void updateById(AgentDefinition agentDefinition) {
        repository.updateById(agentDefinition);
    }

    @Override
    public void updateById(List<AgentDefinition> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentDefinition> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentDefinitionRequest request) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentDefinition::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getName()), AgentDefinition::getName, request.getName())
                    .eq(ObjUtil.isNotEmpty(request.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, request.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(request.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, request.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(request.getSecondRule()), AgentDefinition::getSecondRule, request.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(request.getThirdSkill()), AgentDefinition::getThirdSkill, request.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(request.getModel()), AgentDefinition::getModel, request.getModel())
                    .eq(ObjUtil.isNotEmpty(request.getCreateBy()), AgentDefinition::getCreateBy, request.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(request.getUpdateBy()), AgentDefinition::getUpdateBy, request.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentDefinition::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), AgentDefinition::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentDefinition::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

