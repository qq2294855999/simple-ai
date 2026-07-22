package com.simple.ai.view.chatTurn;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.chatTurn.ChatTurn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话轮次(chat_turn)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface ChatTurnRepository extends BaseMapper<ChatTurn> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<ChatTurn> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ChatTurn> entities);

}

