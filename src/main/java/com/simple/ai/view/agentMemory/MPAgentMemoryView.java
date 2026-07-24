package com.simple.ai.view.agentMemory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemory.DeleteAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体记忆(agent_memory)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentMemoryView implements AgentMemoryView {

    @Autowired
    private AgentMemoryRepository repository;

    @Override
    public IPage<AgentMemory> findAll(PageAgentMemoryRequest pageRequest) {
        Page<AgentMemory> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<AgentMemory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjUtil.isNotEmpty(pageRequest.getAgentId()), AgentMemory::getAgentId, pageRequest.getAgentId())
               .like(ObjUtil.isNotEmpty(pageRequest.getMemoryName()), AgentMemory::getMemoryName, pageRequest.getMemoryName())
               .eq(ObjUtil.isNotEmpty(pageRequest.getVersionStatus()), AgentMemory::getVersionStatus, pageRequest.getVersionStatus())
               .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentMemory::getStatus, pageRequest.getStatus())
               .orderByDesc(AgentMemory::getCreateTime);
        return repository.selectPage(page, wrapper);
    }

    @Override
    public List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindAllWrapper(findAllRequest, neRequest);
        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentMemory findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        List<AgentMemory> list = repository.selectList(queryWrapper);

        // 空结果直接返回空对象引用
        if (list.isEmpty()) {
            return null;
        }

        // 校验条件单查结果唯一性
        if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public void save(AgentMemory agentMemory) {
        repository.insert(agentMemory);
    }

    @Override
    public void updateById(AgentMemory agentMemory) {
        repository.updateById(agentMemory);
    }

    @Override
    public void saves(List<AgentMemory> list) {

        // 空集合不执行批量新增
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentMemoryRequest request) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentMemory::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentId()), AgentMemory::getAgentId, request.getAgentId())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentMemory::getStatus, request.getStatus());
        repository.delete(queryWrapper);
    }

    /**
     * 构建列表查询条件。
     *
     * @param findAllRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentMemory> buildFindAllWrapper(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentMemory::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentId()), AgentMemory::getAgentId, findAllRequest.getAgentId())
                    .like(ObjUtil.isNotEmpty(findAllRequest.getMemoryName()), AgentMemory::getMemoryName, findAllRequest.getMemoryName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getVersionStatus()), AgentMemory::getVersionStatus, findAllRequest.getVersionStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentMemory::getStatus, findAllRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId());
        return queryWrapper;
    }

    /**
     * 构建单条查询条件。
     *
     * @param findOneRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentMemory> buildFindOneWrapper(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentMemory::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentMemory::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMemoryName()), AgentMemory::getMemoryName, findOneRequest.getMemoryName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getVersionStatus()), AgentMemory::getVersionStatus, findOneRequest.getVersionStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentMemory::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId());
        return queryWrapper;
    }
}