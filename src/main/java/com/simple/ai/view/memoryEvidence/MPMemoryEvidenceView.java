package com.simple.ai.view.memoryEvidence;

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
import com.simple.ai.common.view.memoryEvidence.MemoryEvidenceView;
import com.simple.ai.common.entity.memoryEvidence.MemoryEvidence;
import com.simple.ai.common.dto.memoryEvidence.PageMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.FindOneMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.FindAllMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.DeleteMemoryEvidenceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 记忆证据(memory_evidence)数据库视图实现
 *
 * @author qty
 */
@Component
class MPMemoryEvidenceView implements MemoryEvidenceView {

    @Autowired
    private MemoryEvidenceRepository repository;

    @Override
    public IPage<MemoryEvidence> findAll(PageMemoryEvidenceRequest pageRequest) {
        LambdaQueryWrapper<MemoryEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getTurnId()), MemoryEvidence::getTurnId, pageRequest.getTurnId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, pageRequest.getMemoryVersionId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, pageRequest.getEvidenceType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, pageRequest.getEvidenceContent())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getQualityScore()), MemoryEvidence::getQualityScore, pageRequest.getQualityScore())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStatus()), MemoryEvidence::getStatus, pageRequest.getStatus());
        return repository.selectPage(pageRequest.getPage(MemoryEvidence.class), queryWrapper);
    }

    @Override
    public List<MemoryEvidence> findAll(FindAllMemoryEvidenceRequest findAllRequest, FindAllMemoryEvidenceRequest neRequest) {
        LambdaQueryWrapper<MemoryEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), MemoryEvidence::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTurnId()), MemoryEvidence::getTurnId, findAllRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, findAllRequest.getMemoryVersionId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, findAllRequest.getEvidenceType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, findAllRequest.getEvidenceContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getQualityScore()), MemoryEvidence::getQualityScore, findAllRequest.getQualityScore())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), MemoryEvidence::getStatus, findAllRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), MemoryEvidence::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), MemoryEvidence::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, neRequest.getMemoryVersionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, neRequest.getEvidenceType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, neRequest.getEvidenceContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getQualityScore()), MemoryEvidence::getQualityScore, neRequest.getQualityScore())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), MemoryEvidence::getStatus, neRequest.getStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public MemoryEvidence findOne(FindOneMemoryEvidenceRequest findOneRequest, FindOneMemoryEvidenceRequest neRequest) {
        LambdaQueryWrapper<MemoryEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), MemoryEvidence::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnId()), MemoryEvidence::getTurnId, findOneRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, findOneRequest.getMemoryVersionId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, findOneRequest.getEvidenceType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, findOneRequest.getEvidenceContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getQualityScore()), MemoryEvidence::getQualityScore, findOneRequest.getQualityScore())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), MemoryEvidence::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), MemoryEvidence::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), MemoryEvidence::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, neRequest.getMemoryVersionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, neRequest.getEvidenceType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, neRequest.getEvidenceContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getQualityScore()), MemoryEvidence::getQualityScore, neRequest.getQualityScore())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), MemoryEvidence::getStatus, neRequest.getStatus());

        List<MemoryEvidence> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneMemoryEvidenceRequest findOneRequest, FindOneMemoryEvidenceRequest neRequest) {
        LambdaQueryWrapper<MemoryEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), MemoryEvidence::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnId()), MemoryEvidence::getTurnId, findOneRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, findOneRequest.getMemoryVersionId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, findOneRequest.getEvidenceType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, findOneRequest.getEvidenceContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getQualityScore()), MemoryEvidence::getQualityScore, findOneRequest.getQualityScore())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), MemoryEvidence::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), MemoryEvidence::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), MemoryEvidence::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, neRequest.getMemoryVersionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceType()), MemoryEvidence::getEvidenceType, neRequest.getEvidenceType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEvidenceContent()), MemoryEvidence::getEvidenceContent, neRequest.getEvidenceContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getQualityScore()), MemoryEvidence::getQualityScore, neRequest.getQualityScore())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), MemoryEvidence::getStatus, neRequest.getStatus());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public MemoryEvidence findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(MemoryEvidence memoryEvidence) {
        repository.insert(memoryEvidence);
    }

    @Override
    public void updateById(MemoryEvidence memoryEvidence) {
        repository.updateById(memoryEvidence);
    }

    @Override
    public void updateById(List<MemoryEvidence> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<MemoryEvidence> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteMemoryEvidenceRequest request) {
        LambdaQueryWrapper<MemoryEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), MemoryEvidence::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getTurnId()), MemoryEvidence::getTurnId, request.getTurnId())
                    .eq(ObjUtil.isNotEmpty(request.getMemoryVersionId()), MemoryEvidence::getMemoryVersionId, request.getMemoryVersionId())
                    .eq(ObjUtil.isNotEmpty(request.getEvidenceType()), MemoryEvidence::getEvidenceType, request.getEvidenceType())
                    .eq(ObjUtil.isNotEmpty(request.getEvidenceContent()), MemoryEvidence::getEvidenceContent, request.getEvidenceContent())
                    .eq(ObjUtil.isNotEmpty(request.getQualityScore()), MemoryEvidence::getQualityScore, request.getQualityScore())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), MemoryEvidence::getStatus, request.getStatus());
        repository.delete(queryWrapper);
    }
}

