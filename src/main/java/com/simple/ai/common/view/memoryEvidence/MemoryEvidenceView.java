package com.simple.ai.common.view.memoryEvidence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.memoryEvidence.DeleteMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.FindAllMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.FindOneMemoryEvidenceRequest;
import com.simple.ai.common.dto.memoryEvidence.PageMemoryEvidenceRequest;
import com.simple.ai.common.entity.memoryEvidence.MemoryEvidence;

import java.util.List;

/**
 * 记忆证据(memory_evidence)数据库视图接口
 *
 * @author qty
 */
public interface MemoryEvidenceView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<MemoryEvidence> findAll(PageMemoryEvidenceRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return MemoryEvidence 原始表数据
     */
    List<MemoryEvidence> findAll(FindAllMemoryEvidenceRequest findAllRequest, FindAllMemoryEvidenceRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return MemoryEvidence 原始表数据
     */
    MemoryEvidence findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return MemoryEvidence 原始表数据
     */
    MemoryEvidence findOne(FindOneMemoryEvidenceRequest findOneRequest, FindOneMemoryEvidenceRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneMemoryEvidenceRequest findOneRequest, FindOneMemoryEvidenceRequest neRequest);

    /**
     * 新增
     *
     * @param memoryEvidence 记忆证据对象
     */
    void save(MemoryEvidence memoryEvidence);

    /**
     * 根据id修改
     *
     * @param memoryEvidence 记忆证据对象
     */
    void updateById(MemoryEvidence memoryEvidence);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<MemoryEvidence> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<MemoryEvidence> list);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 删除
     *
     * @param request 条件
     */
    void delete(DeleteMemoryEvidenceRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return MemoryEvidence 原始表数据
     */
    default MemoryEvidence findOne(FindOneMemoryEvidenceRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneMemoryEvidenceRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return MemoryEvidence 原始表数据
     */
    default List<MemoryEvidence> findAll(FindAllMemoryEvidenceRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllMemoryEvidenceRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneMemoryEvidenceRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneMemoryEvidenceRequest());
    }

}

