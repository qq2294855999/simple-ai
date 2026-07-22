package com.simple.ai.common.service.chatTurn;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.chatTurn.*;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.chatTurn.ChatTurn;

import java.util.List;

/**
 * 对话轮次(chat_turn)接口
 *
 * @author qty
 */
public interface ChatTurnService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageChatTurnResponse> findAll(PageChatTurnRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return ChatTurnFullInfoResponse  对话轮次 详细数据
     */
    InfoChatTurnResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 对话轮次 请求对象
     */
    String save(CreateChatTurnRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 对话轮次 请求对象
     */
    String updateById(UpdateChatTurnRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 开始新轮次。
     * <p>在用户消息保存后调用，创建本轮对话轮次记录。</p>
     *
     * @param sessionId     会话主键
     * @param userMessageId 用户消息主键
     * @param taskId        调度任务主键
     * @return 新创建的对话轮次
     */
    ChatTurn startTurn(String sessionId, String userMessageId, String taskId);

    /**
     * 完成当前轮次。
     * <p>AI回复消息保存后调用，关联回复消息并写入推理摘要。</p>
     *
     * @param turnId             轮次主键
     * @param assistantMessageId AI回复消息主键
     * @param reasoningSummary   受控推理摘要
     */
    void completeTurn(String turnId, String assistantMessageId, String reasoningSummary);

    /**
     * 加载会话最近N轮完整消息窗口。
     * <p>从最近轮次向前累积，按token上限裁剪，返回ChatML格式的历史上下文。</p>
     *
     * @param sessionId     会话主键
     * @param maxTokens     token上限
     * @param fullTurnCount 保留完整内容的最近轮次数
     * @return ChatML格式的历史消息列表
     */
    List<AgentChatMessage> loadRecentMessages(String sessionId, int maxTokens, int fullTurnCount);
}

