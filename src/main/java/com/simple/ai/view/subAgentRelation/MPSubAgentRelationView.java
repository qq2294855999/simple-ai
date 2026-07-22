package com.simple.ai.view.subAgentRelation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.subAgentRelation.*;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserve()), SubAgentRelation::getReserve, pageRequest.getReserve())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), SubAgentRelation::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(SubAgentRelation.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateSubAgentRelationResponse> findAggregateAll(PageAggregateSubAgentRelationRequest pageRequest) {

        // 构建分页边界
        Page<SubAgentRelation> page = pageRequest.getPage(SubAgentRelation.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合分页记录和总数
        List<PageAggregateSubAgentRelationResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateSubAgentRelationResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<SubAgentRelation> findAll(FindAllSubAgentRelationRequest findAllRequest, FindAllSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = buildFindAllWrapper(findAllRequest, neRequest);
        return repository.selectList(queryWrapper);
    }

    @Override
    public SubAgentRelation findOne(FindOneSubAgentRelationRequest findOneRequest, FindOneSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        List<SubAgentRelation> list = repository.selectList(queryWrapper);

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
    public Long findCount(FindOneSubAgentRelationRequest findOneRequest, FindOneSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
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
    public void delete(DeleteSubAgentRelationRequest request) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), SubAgentRelation::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getMainAgentId()), SubAgentRelation::getMainAgentId, request.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getSubAgentId()), SubAgentRelation::getSubAgentId, request.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), SubAgentRelation::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserve()), SubAgentRelation::getReserve, request.getReserve())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), SubAgentRelation::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }

    /**
     * 构建列表查询条件。
     *
     * @param findAllRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SubAgentRelation> buildFindAllWrapper(FindAllSubAgentRelationRequest findAllRequest, FindAllSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), SubAgentRelation::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, findAllRequest.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, findAllRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), SubAgentRelation::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserve()), SubAgentRelation::getReserve, findAllRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), SubAgentRelation::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentRelation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, neRequest.getMainAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, neRequest.getSubAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), SubAgentRelation::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), SubAgentRelation::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), SubAgentRelation::getRemark, neRequest.getRemark());
        return queryWrapper;
    }

    /**
     * 构建单条查询条件。
     *
     * @param findOneRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SubAgentRelation> buildFindOneWrapper(FindOneSubAgentRelationRequest findOneRequest, FindOneSubAgentRelationRequest neRequest) {
        LambdaQueryWrapper<SubAgentRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), SubAgentRelation::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, findOneRequest.getMainAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, findOneRequest.getSubAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), SubAgentRelation::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserve()), SubAgentRelation::getReserve, findOneRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), SubAgentRelation::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), SubAgentRelation::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMainAgentId()), SubAgentRelation::getMainAgentId, neRequest.getMainAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSubAgentId()), SubAgentRelation::getSubAgentId, neRequest.getSubAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), SubAgentRelation::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), SubAgentRelation::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), SubAgentRelation::getRemark, neRequest.getRemark());
        return queryWrapper;
    }
}


