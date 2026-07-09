package com.simple.ai.service.atomicCommand;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.atomicCommand.AtomicCommandService;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.copy.atomicCommand.AtomicCommandCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 原子命令(atomic_command)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAtomicCommandService implements AtomicCommandService {

    @Autowired
    private AtomicCommandView atomicCommandView;

    @Autowired
    private AtomicCommandCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAtomicCommandResponse> findAll(PageAtomicCommandRequest pageRequest) {
        var pageInfo = atomicCommandView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAtomicCommandResponse findById(String id) {
        var atomicCommand = atomicCommandView.findById(id);
        AssertUtils.notEmpty(atomicCommand, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(atomicCommand);
    }

    @Override
    public String save(CreateAtomicCommandRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        atomicCommandView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAtomicCommandRequest updateRequest) {
        var atomicCommand = atomicCommandView.findById(updateRequest.getId());
        AssertUtils.notEmpty(atomicCommand, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        atomicCommandView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        atomicCommandView.deleteByIds(ids);
    }
}

