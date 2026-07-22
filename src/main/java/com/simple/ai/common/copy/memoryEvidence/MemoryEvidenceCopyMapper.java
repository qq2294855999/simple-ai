package com.simple.ai.common.copy.memoryEvidence;

import java.util.Date;

import com.simple.ai.common.entity.memoryEvidence.MemoryEvidence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.memoryEvidence.PageMemoryEvidenceResponse;
import com.simple.ai.common.dto.memoryEvidence.InfoMemoryEvidenceResponse;
import com.simple.ai.common.dto.memoryEvidence.CreateMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.UpdateMemoryEvidenceRequest;

import java.util.List;

/**
 * 记忆证据(memory_evidence)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface MemoryEvidenceCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageMemoryEvidenceResponse toPageResponse(MemoryEvidence entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoMemoryEvidenceResponse toInfoResponse(MemoryEvidence entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return MemoryEvidence 数据对象
     */
    MemoryEvidence toEntity(CreateMemoryEvidenceRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return MemoryEvidence 数据对象
     */
    MemoryEvidence toEntity(UpdateMemoryEvidenceRequest updateRequest);

}

