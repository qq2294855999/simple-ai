package com.simple.ai.service.memoryEvidence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.memoryEvidence.MemoryEvidenceCopyMapper;
import com.simple.ai.common.dto.memoryEvidence.*;
import com.simple.ai.common.service.memoryEvidence.MemoryEvidenceService;
import com.simple.ai.common.view.memoryEvidence.MemoryEvidenceView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 记忆证据(memory_evidence)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultMemoryEvidenceService implements MemoryEvidenceService {

    @Autowired
    private MemoryEvidenceView memoryEvidenceView;

    @Autowired
    private MemoryEvidenceCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageMemoryEvidenceResponse> findAll(PageMemoryEvidenceRequest pageRequest) {
        var pageInfo = memoryEvidenceView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoMemoryEvidenceResponse findById(String id) {
        var memoryEvidence = memoryEvidenceView.findById(id);
        AssertUtils.notEmpty(memoryEvidence, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(memoryEvidence);
    }

    @Override
    public String save(CreateMemoryEvidenceRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        memoryEvidenceView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateMemoryEvidenceRequest updateRequest) {
        var memoryEvidence = memoryEvidenceView.findById(updateRequest.getId());
        AssertUtils.notEmpty(memoryEvidence, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        memoryEvidenceView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        memoryEvidenceView.deleteByIds(ids);
    }
}

