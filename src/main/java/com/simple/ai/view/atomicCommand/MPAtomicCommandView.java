package com.simple.ai.view.atomicCommand;

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
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.FindOneAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.FindAllAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.DeleteAtomicCommandRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

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
                    .like(ObjUtil.isNotEmpty(pageRequest.getFunc()), AtomicCommand::getFunc, pageRequest.getFunc())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AtomicCommand::getStatus, pageRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getSync()), AtomicCommand::getSync, pageRequest.getSync());
        return repository.selectPage(pageRequest.getPage(AtomicCommand.class), queryWrapper);
    }

    @Override
    public List<AtomicCommand> findAll(FindAllAtomicCommandRequest findAllRequest, FindAllAtomicCommandRequest neRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AtomicCommand::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getName()), AtomicCommand::getName, findAllRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCommand()), AtomicCommand::getCommand, findAllRequest.getCommand())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFunc()), AtomicCommand::getFunc, findAllRequest.getFunc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AtomicCommand::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSync()), AtomicCommand::getSync, findAllRequest.getSync())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFunc()), AtomicCommand::getFunc, neRequest.getFunc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSync()), AtomicCommand::getSync, neRequest.getSync());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AtomicCommand findOne(FindOneAtomicCommandRequest findOneRequest, FindOneAtomicCommandRequest neRequest) {
        LambdaQueryWrapper<AtomicCommand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AtomicCommand::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AtomicCommand::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommand()), AtomicCommand::getCommand, findOneRequest.getCommand())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFunc()), AtomicCommand::getFunc, findOneRequest.getFunc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AtomicCommand::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSync()), AtomicCommand::getSync, findOneRequest.getSync())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFunc()), AtomicCommand::getFunc, neRequest.getFunc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSync()), AtomicCommand::getSync, neRequest.getSync());

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
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFunc()), AtomicCommand::getFunc, findOneRequest.getFunc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AtomicCommand::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSync()), AtomicCommand::getSync, findOneRequest.getSync())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AtomicCommand::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AtomicCommand::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommand()), AtomicCommand::getCommand, neRequest.getCommand())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFunc()), AtomicCommand::getFunc, neRequest.getFunc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AtomicCommand::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSync()), AtomicCommand::getSync, neRequest.getSync());
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
                    .eq(ObjUtil.isNotEmpty(request.getFunc()), AtomicCommand::getFunc, request.getFunc())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AtomicCommand::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getSync()), AtomicCommand::getSync, request.getSync());
        repository.delete(queryWrapper);
    }
}

