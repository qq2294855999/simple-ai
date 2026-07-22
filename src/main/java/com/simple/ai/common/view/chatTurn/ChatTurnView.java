package com.simple.ai.common.view.chatTurn;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.chatTurn.DeleteChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.FindAllChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.FindOneChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.PageChatTurnRequest;
import com.simple.ai.common.entity.chatTurn.ChatTurn;

import java.util.List;

/**
 * 对话轮次(chat_turn)数据库视图接口
 *
 * @author qty
 */
public interface ChatTurnView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<ChatTurn> findAll(PageChatTurnRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return ChatTurn 原始表数据
     */
    List<ChatTurn> findAll(FindAllChatTurnRequest findAllRequest, FindAllChatTurnRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return ChatTurn 原始表数据
     */
    ChatTurn findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return ChatTurn 原始表数据
     */
    ChatTurn findOne(FindOneChatTurnRequest findOneRequest, FindOneChatTurnRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneChatTurnRequest findOneRequest, FindOneChatTurnRequest neRequest);

    /**
     * 新增
     *
     * @param chatTurn 对话轮次对象
     */
    void save(ChatTurn chatTurn);

    /**
     * 根据id修改
     *
     * @param chatTurn 对话轮次对象
     */
    void updateById(ChatTurn chatTurn);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<ChatTurn> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<ChatTurn> list);

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
    void delete(DeleteChatTurnRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return ChatTurn 原始表数据
     */
    default ChatTurn findOne(FindOneChatTurnRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneChatTurnRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return ChatTurn 原始表数据
     */
    default List<ChatTurn> findAll(FindAllChatTurnRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllChatTurnRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneChatTurnRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneChatTurnRequest());
    }

}

