package com.simple.ai.view.agentSkill;

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
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.FindOneAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.FindAllAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.DeleteAgentSkillRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 智能体技能(agent_skill)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentSkillView implements AgentSkillView {

    @Autowired
    private AgentSkillRepository repository;

    @Override
    public IPage<AgentSkill> findAll(PageAgentSkillRequest pageRequest) {
        LambdaQueryWrapper<AgentSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getAgentId()), AgentSkill::getAgentId, pageRequest.getAgentId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, pageRequest.getDefinitionDesc())
                    .like(ObjUtil.isNotEmpty(pageRequest.getExecContent()), AgentSkill::getExecContent, pageRequest.getExecContent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, pageRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentSkill::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AgentSkill::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentSkill::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentSkill.class), queryWrapper);
    }

    @Override
    public List<AgentSkill> findAll(FindAllAgentSkillRequest findAllRequest, FindAllAgentSkillRequest neRequest) {
        LambdaQueryWrapper<AgentSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentSkill::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentId()), AgentSkill::getAgentId, findAllRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, findAllRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecContent()), AgentSkill::getExecContent, findAllRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, findAllRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentSkill::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AgentSkill::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentSkill::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentSkill::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentSkill::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentSkill::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentSkill::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentSkill::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentSkill::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentSkill findOne(FindOneAgentSkillRequest findOneRequest, FindOneAgentSkillRequest neRequest) {
        LambdaQueryWrapper<AgentSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentSkill::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentSkill::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecContent()), AgentSkill::getExecContent, findOneRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, findOneRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentSkill::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentSkill::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentSkill::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentSkill::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentSkill::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentSkill::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentSkill::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentSkill::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentSkill::getRemark, neRequest.getRemark());

        List<AgentSkill> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentSkillRequest findOneRequest, FindOneAgentSkillRequest neRequest) {
        LambdaQueryWrapper<AgentSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentSkill::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentSkill::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecContent()), AgentSkill::getExecContent, findOneRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, findOneRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentSkill::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentSkill::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentSkill::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentSkill::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentSkill::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentSkill::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentSkill::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentSkill::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentSkill::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentSkill::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentSkill::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AgentSkill findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentSkill agentSkill) {
        repository.insert(agentSkill);
    }

    @Override
    public void updateById(AgentSkill agentSkill) {
        repository.updateById(agentSkill);
    }

    @Override
    public void updateById(List<AgentSkill> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentSkill> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentSkillRequest request) {
        LambdaQueryWrapper<AgentSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentSkill::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentId()), AgentSkill::getAgentId, request.getAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getDefinitionDesc()), AgentSkill::getDefinitionDesc, request.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(request.getExecContent()), AgentSkill::getExecContent, request.getExecContent())
                    .eq(ObjUtil.isNotEmpty(request.getReturnDataFormat()), AgentSkill::getReturnDataFormat, request.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentSkill::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), AgentSkill::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentSkill::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

