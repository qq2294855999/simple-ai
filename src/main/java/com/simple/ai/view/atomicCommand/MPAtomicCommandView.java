package com.simple.ai.view.atomicCommand;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.atomicCommand.*;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 原子命令(atomic_command)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAtomicCommandView implements AtomicCommandView {

    @Autowired
    private AtomicCommandRepository repository;

    @Override
    public IPage<AtomicCommand> findAll(PageAtomicCommandRequest pageRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getName()), AtomicCommand::getName, pageRequest.getName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getCommand()), AtomicCommand::getCommand, pageRequest.getCommand())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRole()), AtomicCommand::getRole, pageRequest.getRole())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getSkillId()), AtomicCommand::getSkillId, pageRequest.getSkillId())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AtomicCommand::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AtomicCommand::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AtomicCommand::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AtomicCommand.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateAtomicCommandResponse> findAggregateAll(PageAggregateAtomicCommandRequest pageRequest) {

        // 构建分页边界
        Page<AtomicCommand> page = pageRequest.getPage(AtomicCommand.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合记录与总数
        List<PageAggregateAtomicCommandResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateAtomicCommandResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AtomicCommand> findAll(FindAllAtomicCommandRequest findAllRequest, FindAllAtomicCommandRequest neRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AtomicCommand::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getName()), AtomicCommand::getName, findAllRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCommand()), AtomicCommand::getCommand, findAllRequest.getCommand())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRole()), AtomicCommand::getRole, findAllRequest.getRole())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSkillId()), AtomicCommand::getSkillId, findAllRequest.getSkillId())
                    .in(CollectionUtil.isNotEmpty(findAllRequest.getSkillIds()), AtomicCommand::getSkillId, findAllRequest.getSkillIds())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AtomicCommand::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AtomicCommand::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AtomicCommand::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRole()), AtomicCommand::getRole, neRequest.getRole())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSkillId()), AtomicCommand::getSkillId, neRequest.getSkillId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AtomicCommand::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AtomicCommand::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AtomicCommand findOne(FindOneAtomicCommandRequest findOneRequest, FindOneAtomicCommandRequest neRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AtomicCommand::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AtomicCommand::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommand()), AtomicCommand::getCommand, findOneRequest.getCommand())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRole()), AtomicCommand::getRole, findOneRequest.getRole())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSkillId()), AtomicCommand::getSkillId, findOneRequest.getSkillId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AtomicCommand::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AtomicCommand::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AtomicCommand::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRole()), AtomicCommand::getRole, neRequest.getRole())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSkillId()), AtomicCommand::getSkillId, neRequest.getSkillId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AtomicCommand::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AtomicCommand::getRemark, neRequest.getRemark());

        List<AtomicCommand> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAtomicCommandRequest findOneRequest, FindOneAtomicCommandRequest neRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AtomicCommand::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AtomicCommand::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommand()), AtomicCommand::getCommand, findOneRequest.getCommand())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRole()), AtomicCommand::getRole, findOneRequest.getRole())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSkillId()), AtomicCommand::getSkillId, findOneRequest.getSkillId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AtomicCommand::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AtomicCommand::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AtomicCommand::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRole()), AtomicCommand::getRole, neRequest.getRole())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSkillId()), AtomicCommand::getSkillId, neRequest.getSkillId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AtomicCommand::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AtomicCommand::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AtomicCommand findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AtomicCommand atomicCommand) {
        repository.insert(atomicCommand);
    }

    @Override
    public void updateById(AtomicCommand atomicCommand) {
        repository.updateById(atomicCommand);
    }

    @Override
    public void updateById(List<AtomicCommand> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AtomicCommand> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAtomicCommandRequest request) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AtomicCommand::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getName()), AtomicCommand::getName, request.getName())
                    .eq(ObjUtil.isNotEmpty(request.getCommand()), AtomicCommand::getCommand, request.getCommand())
                    .eq(ObjUtil.isNotEmpty(request.getRole()), AtomicCommand::getRole, request.getRole())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AtomicCommand::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), AtomicCommand::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AtomicCommand::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

