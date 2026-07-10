package com.simple.ai.view.atomicCommand;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 原子命令(atomic_command)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface AtomicCommandRepository extends BaseMapper<AtomicCommand> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<AtomicCommand> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AtomicCommand> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateAtomicCommandResponse> selectAggregatePage(@Param("pageRequest") PageAggregateAtomicCommandRequest pageRequest,
                                                                  @Param("offset") Long offset,
                                                                  @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateAtomicCommandRequest pageRequest);

}

