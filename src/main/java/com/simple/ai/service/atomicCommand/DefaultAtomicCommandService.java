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
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandResponse;
import com.simple.ai.common.copy.atomicCommand.AtomicCommandCopyMapper;
import com.simple.common.mp.common.enums.Status;

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
    private AgentSkillView agentSkillView;

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
    public IPage<PageAggregateAtomicCommandResponse> findAggregateAll(PageAggregateAtomicCommandRequest pageRequest) {

        // 查询原子命令聚合分页数据
        return atomicCommandView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAtomicCommandResponse findById(String id) {
        var atomicCommand = atomicCommandView.findById(id);
        AssertUtils.notEmpty(atomicCommand, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(atomicCommand);
    }

    @Override
    public String save(CreateAtomicCommandRequest createRequest) {

        // 若指定技能，校验技能存在
        if (createRequest.getSkillId() != null && !createRequest.getSkillId().isEmpty()) {
            AgentSkill skill = agentSkillView.findById(createRequest.getSkillId());
            AssertUtils.notEmpty(skill, "技能[{}]不存在", createRequest.getSkillId());
        }

        // 构建并保存原子命令
        var entity = copy.toEntity(createRequest);
        entity.setStatus(Status.ON);
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

