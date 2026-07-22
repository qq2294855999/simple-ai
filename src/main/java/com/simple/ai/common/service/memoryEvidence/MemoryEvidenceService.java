package com.simple.ai.common.service.memoryEvidence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.memoryEvidence.*;

import java.util.List;

/**
 * 记忆证据(memory_evidence)接口
 *
 * @author qty
 */
public interface MemoryEvidenceService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageMemoryEvidenceResponse> findAll(PageMemoryEvidenceRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return MemoryEvidenceFullInfoResponse  记忆证据 详细数据
     */
    InfoMemoryEvidenceResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 记忆证据 请求对象
     */
    String save(CreateMemoryEvidenceRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 记忆证据 请求对象
     */
    String updateById(UpdateMemoryEvidenceRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

