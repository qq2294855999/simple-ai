package com.simple.ai.common.copy.protocol;

import com.simple.ai.common.dto.protocol.CreateProtocolRequest;
import com.simple.ai.common.dto.protocol.InfoProtocolResponse;
import com.simple.ai.common.dto.protocol.PageProtocolResponse;
import com.simple.ai.common.dto.protocol.UpdateProtocolRequest;
import com.simple.ai.common.entity.protocol.Protocol;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 执行器协议(agent_protocol)对象属性复制。
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface ProtocolCopyMapper {

    /**
     * 将数据对象赋值到page返回对象。
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageProtocolResponse toPageResponse(Protocol entity);

    /**
     * 将数据对象列表赋值到page返回对象列表。
     *
     * @param entities 数据对象列表
     * @return page 数据列表
     */
    List<PageProtocolResponse> toPageResponseList(List<Protocol> entities);

    /**
     * 将数据对象赋值到info返回对象。
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoProtocolResponse toInfoResponse(Protocol entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象。
     *
     * @param createRequest 创建接收数据对象
     * @return Protocol 数据对象
     */
    Protocol toEntity(CreateProtocolRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象。
     *
     * @param updateRequest 创建接收数据对象
     * @return Protocol 数据对象
     */
    Protocol toEntity(UpdateProtocolRequest updateRequest);

}