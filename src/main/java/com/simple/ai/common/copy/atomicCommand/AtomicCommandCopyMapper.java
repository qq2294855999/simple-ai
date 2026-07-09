package com.simple.ai.common.copy.atomicCommand;

import java.util.Date;

import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;

import java.util.List;

/**
 * 原子命令(atomic_command)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AtomicCommandCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAtomicCommandResponse toPageResponse(AtomicCommand entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAtomicCommandResponse toInfoResponse(AtomicCommand entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AtomicCommand 数据对象
     */
    AtomicCommand toEntity(CreateAtomicCommandRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AtomicCommand 数据对象
     */
    AtomicCommand toEntity(UpdateAtomicCommandRequest updateRequest);

}

