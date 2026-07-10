package com.simple.ai.view.agentMemory;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryResponse;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 鏅鸿兘浣撹蹇?agent_memory)鏁版嵁搴撹闂眰
 *
 * @author qty
 */
@Mapper
public interface AgentMemoryRepository extends BaseMapper<AgentMemory> {

    /**
     * 鎵归噺鏂板鏁版嵁銆?     *
     * @param entities List<AgentMemory> 瀹炰緥瀵硅薄鍒楄〃
     * @return 褰卞搷琛屾暟
     */
    int insertBatch(@Param("entities") List<AgentMemory> entities);

    /**
     * 鏌ヨ鑱氬悎鍒嗛〉鍒楄〃銆?     *
     * @param pageRequest 鍒嗛〉璇锋眰
     * @param offset 鍋忕Щ閲?     * @param size 姣忛〉鏁伴噺
     * @return 鑱氬悎鍒嗛〉鍒楄〃
     */
    List<PageAggregateAgentMemoryResponse> selectAggregatePage(@Param("pageRequest") PageAggregateAgentMemoryRequest pageRequest,
                                                               @Param("offset") Long offset,
                                                               @Param("size") Long size);

    /**
     * 鏌ヨ鑱氬悎鍒嗛〉鎬绘暟銆?     *
     * @param pageRequest 鍒嗛〉璇锋眰
     * @return 鎬绘暟
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateAgentMemoryRequest pageRequest);

    /**
     * 按记忆主键批量删除关联的任务详情。
     *
     * @param memoryIds 记忆主键列表
     * @return 影响行数
     */
    int deleteTaskDetailsByMemoryIds(@Param("memoryIds") List<String> memoryIds);

    /**
     * 按记忆主键批量删除关联的任务。
     *
     * @param memoryIds 记忆主键列表
     * @return 影响行数
     */
    int deleteTasksByMemoryIds(@Param("memoryIds") List<String> memoryIds);

    /**
     * 按记忆主键批量删除关联的记忆详情。
     *
     * @param memoryIds 记忆主键列表
     * @return 影响行数
     */
    int deleteMemoryDetailsByMemoryIds(@Param("memoryIds") List<String> memoryIds);
}
