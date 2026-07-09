package com.simple.ai.view.subAgentRelation;

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
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.FindOneSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.FindAllSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.DeleteSubAgentRelationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 子智能体关联(sub_agent_relation)数据库视图实现
 *
 * @author qty
 */
@Component
class MPSubAgentRelationView implements SubAgentRelationView {

    @Autowired
    private SubAgentRelationRepository repository;

    @Override
    public IPage<SubAgentRelation> findAll(PageSubAgentRelationRequest pageRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, pageRequest.getMainAgentId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, pageRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), SubAgentRelation::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), SubAgentRelation::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), SubAgentRelation::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(SubAgentRelation.class), queryWrapper);
    }

    @Override
    public List<SubAgentRelation> findAll(FindAllSubAgentRelationRequest findAllRequest, FindAllSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), SubAgentRelation::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, findAllRequest.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, findAllRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), SubAgentRelation::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), SubAgentRelation::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), SubAgentRelation::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentRelation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, neRequest.getMainAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, neRequest.getSubAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), SubAgentRelation::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), SubAgentRelation::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), SubAgentRelation::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public SubAgentRelation findOne(FindOneSubAgentRelationRequest findOneRequest, FindOneSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), SubAgentRelation::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, findOneRequest.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, findOneRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), SubAgentRelation::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), SubAgentRelation::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), SubAgentRelation::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentRelation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, neRequest.getMainAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, neRequest.getSubAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), SubAgentRelation::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), SubAgentRelation::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), SubAgentRelation::getRemark, neRequest.getRemark());

        List<SubAgentRelation> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneSubAgentRelationRequest findOneRequest, FindOneSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), SubAgentRelation::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, findOneRequest.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, findOneRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), SubAgentRelation::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), SubAgentRelation::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), SubAgentRelation::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentRelation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, neRequest.getMainAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, neRequest.getSubAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), SubAgentRelation::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), SubAgentRelation::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), SubAgentRelation::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public SubAgentRelation findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(SubAgentRelation subAgentRelation) {
        repository.insert(subAgentRelation);
    }

    @Override
    public void updateById(SubAgentRelation subAgentRelation) {
        repository.updateById(subAgentRelation);
    }

    @Override
    public void updateById(List<SubAgentRelation> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<SubAgentRelation> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteSubAgentRelationRequest request) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), SubAgentRelation::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getMainAgentId()), SubAgentRelation::getMainAgentId, request.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getSubAgentId()), SubAgentRelation::getSubAgentId, request.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), SubAgentRelation::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), SubAgentRelation::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), SubAgentRelation::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

