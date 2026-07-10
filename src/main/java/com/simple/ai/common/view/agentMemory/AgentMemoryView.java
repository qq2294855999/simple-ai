package com.simple.ai.common.view.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.DeleteAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryResponse;
import com.simple.ai.common.entity.agentMemory.AgentMemory;

import java.util.List;

/**
 * Agent memory view interface.
 *
 * @author qty
 */
public interface AgentMemoryView {

    /**
     * Page list.
     *
     * @param pageRequest page request
     * @return page data
     */
    IPage<AgentMemory> findAll(PageAgentMemoryRequest pageRequest);

    /**
     * Aggregate page list.
     *
     * @param pageRequest page request
     * @return aggregate page data
     */
    IPage<PageAggregateAgentMemoryResponse> findAggregateAll(PageAggregateAgentMemoryRequest pageRequest);

    /**
     * Find list.
     *
     * @param findAllRequest query request
     * @param neRequest exclude request
     * @return entity list
     */
    List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest);

    /**
     * Find by id.
     *
     * @param id primary key
     * @return entity data
     */
    AgentMemory findById(String id);

    /**
     * Find one.
     *
     * @param findOneRequest query request
     * @param neRequest exclude request
     * @return entity data
     */
    AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest);

    /**
     * Count.
     *
     * @param findOneRequest query request
     * @param neRequest exclude request
     * @return count
     */
    Long findCount(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest);

    /**
     * Save entity.
     *
     * @param agentMemory entity
     */
    void save(AgentMemory agentMemory);

    /**
     * Update by id.
     *
     * @param agentMemory entity
     */
    void updateById(AgentMemory agentMemory);

    /**
     * Batch update by id.
     *
     * @param list entity list
     */
    void updateById(List<AgentMemory> list);

    /**
     * Batch save.
     *
     * @param list entity list
     */
    void saves(List<AgentMemory> list);

    /**
     * Delete by ids.
     *
     * @param ids primary keys
     */
    void deleteByIds(List<String> ids);

    /**
     * Delete by condition.
     *
     * @param request condition
     */
    void delete(DeleteAgentMemoryRequest request);

    /**
     * Find one.
     *
     * @param findOneRequest query request
     * @return entity data
     */
    default AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentMemoryRequest());
    }

    /**
     * Find list.
     *
     * @param findAllRequest query request
     * @return entity list
     */
    default List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentMemoryRequest());
    }

    /**
     * Count.
     *
     * @param findOneRequest query request
     * @return count
     */
    default Long findCount(FindOneAgentMemoryRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentMemoryRequest());
    }
}