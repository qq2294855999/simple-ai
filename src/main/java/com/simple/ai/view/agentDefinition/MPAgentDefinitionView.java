package com.simple.ai.view.agentDefinition;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentDefinition.*;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体定义(agent_definition)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentDefinitionView implements AgentDefinitionView {

    @Autowired
    private AgentDefinitionRepository repository;

    @Override
    public IPage<AgentDefinition> findAll(PageAgentDefinitionRequest pageRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getName()), AgentDefinition::getName, pageRequest.getName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, pageRequest.getDefinitionDesc())
                    .like(ObjUtil.isNotEmpty(pageRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, pageRequest.getFirstPrinciple())
                    .like(ObjUtil.isNotEmpty(pageRequest.getSecondRule()), AgentDefinition::getSecondRule, pageRequest.getSecondRule())
                    .like(ObjUtil.isNotEmpty(pageRequest.getThirdSkill()), AgentDefinition::getThirdSkill, pageRequest.getThirdSkill())
                    .like(ObjUtil.isNotEmpty(pageRequest.getModel()), AgentDefinition::getModel, pageRequest.getModel())
                    .like(ObjUtil.isNotEmpty(pageRequest.getCreateBy()), AgentDefinition::getCreateBy, pageRequest.getCreateBy())
                    .like(ObjUtil.isNotEmpty(pageRequest.getUpdateBy()), AgentDefinition::getUpdateBy, pageRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentDefinition::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserve()), AgentDefinition::getReserve, pageRequest.getReserve())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentDefinition::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentDefinition.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateAgentDefinitionResponse> findAggregateAll(PageAggregateAgentDefinitionRequest pageRequest) {

        // 构建分页边界
        Page<AgentDefinition> page = pageRequest.getPage(AgentDefinition.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合分页记录和总数
        List<PageAggregateAgentDefinitionResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateAgentDefinitionResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest, FindAllAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = buildFindAllWrapper(findAllRequest, neRequest);
        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentDefinition findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public InfoAggregateAgentDefinitionResponse findAggregateById(String id) {
        return repository.selectAggregateById(id);
    }

    @Override
    public List<PageAgentSkillResponse> findSkillsByAgentId(String agentId) {
        return repository.selectSkillsByAgentId(agentId);
    }

    @Override
    public List<PageAgentRuleResponse> findRulesByAgentId(String agentId) {
        return repository.selectRulesByAgentId(agentId);
    }

    @Override
    public List<PageSubAgentRelationResponse> findSubAgentRelationsByAgentId(String agentId) {
        return repository.selectSubAgentRelationsByAgentId(agentId);
    }

    @Override
    public List<PageAgentMemoryResponse> findMemoriesByAgentId(String agentId) {
        return repository.selectMemoriesByAgentId(agentId);
    }

    @Override
    public List<PageAgentMemoryDetailResponse> findMemoryDetailsByAgentId(String agentId) {
        return repository.selectMemoryDetailsByAgentId(agentId);
    }

    @Override
    public List<PageTaskResponse> findTasksByAgentId(String agentId) {
        return repository.selectTasksByAgentId(agentId);
    }

    @Override
    public List<PageAtomicCommandResponse> findAtomicCommandsByAgentId(String agentId) {
        return repository.selectAtomicCommandsByAgentId(agentId);
    }

    @Override
    public DeleteCascadeAgentDefinitionResponse countCascadeByIds(List<String> ids) {
        return repository.countCascadeByIds(ids);
    }

    @Override
    public int unlinkAtomicCommandSkillByAgentIds(List<String> ids) {
        return repository.unlinkAtomicCommandSkillByAgentIds(ids);
    }

    @Override
    public int deleteTaskDetailByAgentIds(List<String> ids) {
        return repository.deleteTaskDetailByAgentIds(ids);
    }

    @Override
    public int deleteTaskByAgentIds(List<String> ids) {
        return repository.deleteTaskByAgentIds(ids);
    }

    @Override
    public int deleteMemoryDetailByAgentIds(List<String> ids) {
        return repository.deleteMemoryDetailByAgentIds(ids);
    }

    @Override
    public int deleteMemoryByAgentIds(List<String> ids) {
        return repository.deleteMemoryByAgentIds(ids);
    }

    @Override
    public int deleteSubAgentRelationByAgentIds(List<String> ids) {
        return repository.deleteSubAgentRelationByAgentIds(ids);
    }

    @Override
    public int deleteRuleByAgentIds(List<String> ids) {
        return repository.deleteRuleByAgentIds(ids);
    }

    @Override
    public int deleteSkillByAgentIds(List<String> ids) {
        return repository.deleteSkillByAgentIds(ids);
    }

    @Override
    public int deleteChatMessageByAgentIds(List<String> ids) {
        return repository.deleteChatMessageByAgentIds(ids);
    }

    @Override
    public int deleteChatSessionByAgentIds(List<String> ids) {
        return repository.deleteChatSessionByAgentIds(ids);
    }

    @Override
    public AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        List<AgentDefinition> list = repository.selectList(queryWrapper);

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
    public Long findCount(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        return repository.selectCount(queryWrapper);
    }

    @Override
    public void save(AgentDefinition agentDefinition) {
        repository.insert(agentDefinition);
    }

    @Override
    public void updateById(AgentDefinition agentDefinition) {
        repository.updateById(agentDefinition);
    }

    @Override
    public void updateById(List<AgentDefinition> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentDefinition> list) {

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
    public void delete(DeleteAgentDefinitionRequest request) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentDefinition::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getName()), AgentDefinition::getName, request.getName())
                    .eq(ObjUtil.isNotEmpty(request.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, request.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(request.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, request.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(request.getSecondRule()), AgentDefinition::getSecondRule, request.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(request.getThirdSkill()), AgentDefinition::getThirdSkill, request.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(request.getModel()), AgentDefinition::getModel, request.getModel())
                    .eq(ObjUtil.isNotEmpty(request.getCreateBy()), AgentDefinition::getCreateBy, request.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(request.getUpdateBy()), AgentDefinition::getUpdateBy, request.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentDefinition::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserve()), AgentDefinition::getReserve, request.getReserve())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentDefinition::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }

    /**
     * 构建列表查询条件。
     *
     * @param findAllRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentDefinition> buildFindAllWrapper(FindAllAgentDefinitionRequest findAllRequest, FindAllAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentDefinition::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getName()), AgentDefinition::getName, findAllRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, findAllRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, findAllRequest.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSecondRule()), AgentDefinition::getSecondRule, findAllRequest.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getThirdSkill()), AgentDefinition::getThirdSkill, findAllRequest.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getModel()), AgentDefinition::getModel, findAllRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getDefaultModelId()), AgentDefinition::getDefaultModelId, findAllRequest.getDefaultModelId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCreateBy()), AgentDefinition::getCreateBy, findAllRequest.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getUpdateBy()), AgentDefinition::getUpdateBy, findAllRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentDefinition::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserve()), AgentDefinition::getReserve, findAllRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentDefinition::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentDefinition::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentDefinition::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, neRequest.getFirstPrinciple())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSecondRule()), AgentDefinition::getSecondRule, neRequest.getSecondRule())
                    .ne(ObjUtil.isNotEmpty(neRequest.getThirdSkill()), AgentDefinition::getThirdSkill, neRequest.getThirdSkill())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentDefinition::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCreateBy()), AgentDefinition::getCreateBy, neRequest.getCreateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUpdateBy()), AgentDefinition::getUpdateBy, neRequest.getUpdateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentDefinition::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), AgentDefinition::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentDefinition::getRemark, neRequest.getRemark());
        return queryWrapper;
    }

    /**
     * 构建单条查询条件。
     *
     * @param findOneRequest 查询条件
     * @param neRequest 排除条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AgentDefinition> buildFindOneWrapper(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest) {
        LambdaQueryWrapper<AgentDefinition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentDefinition::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getName()), AgentDefinition::getName, findOneRequest.getName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefaultModelId()), AgentDefinition::getDefaultModelId, findOneRequest.getDefaultModelId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, findOneRequest.getDefinitionDesc())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, findOneRequest.getFirstPrinciple())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSecondRule()), AgentDefinition::getSecondRule, findOneRequest.getSecondRule())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getThirdSkill()), AgentDefinition::getThirdSkill, findOneRequest.getThirdSkill())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModel()), AgentDefinition::getModel, findOneRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCreateBy()), AgentDefinition::getCreateBy, findOneRequest.getCreateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getUpdateBy()), AgentDefinition::getUpdateBy, findOneRequest.getUpdateBy())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentDefinition::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserve()), AgentDefinition::getReserve, findOneRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentDefinition::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentDefinition::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getName()), AgentDefinition::getName, neRequest.getName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getDefinitionDesc()), AgentDefinition::getDefinitionDesc, neRequest.getDefinitionDesc())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFirstPrinciple()), AgentDefinition::getFirstPrinciple, neRequest.getFirstPrinciple())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSecondRule()), AgentDefinition::getSecondRule, neRequest.getSecondRule())
                    .ne(ObjUtil.isNotEmpty(neRequest.getThirdSkill()), AgentDefinition::getThirdSkill, neRequest.getThirdSkill())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentDefinition::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCreateBy()), AgentDefinition::getCreateBy, neRequest.getCreateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUpdateBy()), AgentDefinition::getUpdateBy, neRequest.getUpdateBy())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentDefinition::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), AgentDefinition::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentDefinition::getRemark, neRequest.getRemark());
        return queryWrapper;
    }
}
