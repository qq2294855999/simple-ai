package com.simple.ai.view.memoryEvidence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.memoryEvidence.MemoryEvidence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 记忆证据(memory_evidence)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface MemoryEvidenceRepository extends BaseMapper<MemoryEvidence> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<MemoryEvidence> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<MemoryEvidence> entities);

}

