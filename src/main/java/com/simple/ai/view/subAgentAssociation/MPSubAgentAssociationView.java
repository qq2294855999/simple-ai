package com.simple.ai.view.subAgentAssociation;

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
import com.simple.ai.common.view.subAgentAssociation.SubAgentAssociationView;
import com.simple.ai.common.entity.subAgentAssociation.SubAgentAssociation;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.FindOneSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.FindAllSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.DeleteSubAgentAssociationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 子智能体关联(sub_agent_association)数据库视图实现
 *
 * @author qty
 */
@Component
class MPSubAgentAssociationView implements SubAgentAssociationView {

    @Autowired
    private SubAgentAssociationRepository repository;

    @Override
    public IPage<SubAgentAssociation> findAll(PageSubAgentAssociationRequest pageRequest) {
        LambdaQueryWrapper<SubAgentAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getMainAgent()), SubAgentAssociation::getMainAgent, pageRequest.getMainAgent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getSubAgent()), SubAgentAssociation::getSubAgent, pageRequest.getSubAgent());
        return repository.selectPage(pageRequest.getPage(SubAgentAssociation.class), queryWrapper);
    }

    @Override
    public List<SubAgentAssociation> findAll(FindAllSubAgentAssociationRequest findAllRequest, FindAllSubAgentAssociationRequest neRequest) {
        LambdaQueryWrapper<SubAgentAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), SubAgentAssociation::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getMainAgent()), SubAgentAssociation::getMainAgent, findAllRequest.getMainAgent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSubAgent()), SubAgentAssociation::getSubAgent, findAllRequest.getSubAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentAssociation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgent()), SubAgentAssociation::getMainAgent, neRequest.getMainAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgent()), SubAgentAssociation::getSubAgent, neRequest.getSubAgent());

        return repository.selectList(queryWrapper);
    }

    @Override
    public SubAgentAssociation findOne(FindOneSubAgentAssociationRequest findOneRequest, FindOneSubAgentAssociationRequest neRequest) {
        LambdaQueryWrapper<SubAgentAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), SubAgentAssociation::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMainAgent()), SubAgentAssociation::getMainAgent, findOneRequest.getMainAgent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSubAgent()), SubAgentAssociation::getSubAgent, findOneRequest.getSubAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentAssociation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgent()), SubAgentAssociation::getMainAgent, neRequest.getMainAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgent()), SubAgentAssociation::getSubAgent, neRequest.getSubAgent());

        List<SubAgentAssociation> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneSubAgentAssociationRequest findOneRequest, FindOneSubAgentAssociationRequest neRequest) {
        LambdaQueryWrapper<SubAgentAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), SubAgentAssociation::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMainAgent()), SubAgentAssociation::getMainAgent, findOneRequest.getMainAgent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSubAgent()), SubAgentAssociation::getSubAgent, findOneRequest.getSubAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentAssociation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgent()), SubAgentAssociation::getMainAgent, neRequest.getMainAgent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgent()), SubAgentAssociation::getSubAgent, neRequest.getSubAgent());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public SubAgentAssociation findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(SubAgentAssociation subAgentAssociation) {
        repository.insert(subAgentAssociation);
    }

    @Override
    public void updateById(SubAgentAssociation subAgentAssociation) {
        repository.updateById(subAgentAssociation);
    }

    @Override
    public void updateById(List<SubAgentAssociation> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<SubAgentAssociation> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteSubAgentAssociationRequest request) {
        LambdaQueryWrapper<SubAgentAssociation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), SubAgentAssociation::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getMainAgent()), SubAgentAssociation::getMainAgent, request.getMainAgent())
                    .eq(ObjUtil.isNotEmpty(request.getSubAgent()), SubAgentAssociation::getSubAgent, request.getSubAgent());
        repository.delete(queryWrapper);
    }
}

