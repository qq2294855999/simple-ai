package com.simple.ai.view.atomicCommand;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
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

}

