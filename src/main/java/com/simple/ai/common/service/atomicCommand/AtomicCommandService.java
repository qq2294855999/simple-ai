package com.simple.ai.common.service.atomicCommand;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandResponse;

/**
 * 原子命令(atomic_command)接口
 *
 * @author qty
 */
public interface AtomicCommandService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAtomicCommandResponse> findAll(PageAtomicCommandRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 请求参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAtomicCommandResponse> findAggregateAll(PageAggregateAtomicCommandRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AtomicCommandFullInfoResponse  原子命令 详细数据
     */
    InfoAtomicCommandResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 原子命令 请求对象
     */
    String save(CreateAtomicCommandRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 原子命令 请求对象
     */
    String updateById(UpdateAtomicCommandRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

