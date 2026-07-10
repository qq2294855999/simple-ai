package com.simple.ai.common.view.atomicCommand;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.FindOneAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.FindAllAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.DeleteAtomicCommandRequest;

/**
 * 原子命令(atomic_command)数据库视图接口
 *
 * @author qty
 */
public interface AtomicCommandView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AtomicCommand> findAll(PageAtomicCommandRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 聚合分页请求
     * @return 聚合分页数据
     */
    IPage<PageAggregateAtomicCommandResponse> findAggregateAll(PageAggregateAtomicCommandRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AtomicCommand 原始表数据
     */
    List<AtomicCommand> findAll(FindAllAtomicCommandRequest findAllRequest, FindAllAtomicCommandRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AtomicCommand 原始表数据
     */
    AtomicCommand findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AtomicCommand 原始表数据
     */
    AtomicCommand findOne(FindOneAtomicCommandRequest findOneRequest, FindOneAtomicCommandRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAtomicCommandRequest findOneRequest, FindOneAtomicCommandRequest neRequest);

    /**
     * 新增
     *
     * @param atomicCommand 原子命令对象
     */
    void save(AtomicCommand atomicCommand);

    /**
     * 根据id修改
     *
     * @param atomicCommand 原子命令对象
     */
    void updateById(AtomicCommand atomicCommand);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AtomicCommand> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AtomicCommand> list);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 删除
     *
     * @param request 条件
     */
    void delete(DeleteAtomicCommandRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AtomicCommand 原始表数据
     */
    default AtomicCommand findOne(FindOneAtomicCommandRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAtomicCommandRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AtomicCommand 原始表数据
     */
    default List<AtomicCommand> findAll(FindAllAtomicCommandRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAtomicCommandRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAtomicCommandRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAtomicCommandRequest());
    }

}

