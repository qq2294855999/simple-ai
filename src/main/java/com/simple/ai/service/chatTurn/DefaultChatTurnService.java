package com.simple.ai.service.chatTurn;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.chatTurn.ChatTurnCopyMapper;
import com.simple.ai.common.dto.chatTurn.*;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.chatTurn.ChatTurn;
import com.simple.ai.common.service.chatTurn.ChatTurnService;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.chatTurn.ChatTurnView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话轮次(chat_turn)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultChatTurnService implements ChatTurnService {

    @Autowired
    private ChatTurnView chatTurnView;

    @Autowired
    private ChatTurnCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    /**
     * 聊天消息视图，用于加载会话历史消息
     */
    @Autowired
    private AgentChatMessageView agentChatMessageView;

    @Override
    public IPage<PageChatTurnResponse> findAll(PageChatTurnRequest pageRequest) {
        var pageInfo = chatTurnView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoChatTurnResponse findById(String id) {
        var chatTurn = chatTurnView.findById(id);
        AssertUtils.notEmpty(chatTurn, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(chatTurn);
    }

    @Override
    public String save(CreateChatTurnRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        chatTurnView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateChatTurnRequest updateRequest) {
        var chatTurn = chatTurnView.findById(updateRequest.getId());
        AssertUtils.notEmpty(chatTurn, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        chatTurnView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        chatTurnView.deleteByIds(ids);
    }

    @Override
    public ChatTurn startTurn(String sessionId, String userMessageId, String taskId) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");
        AssertUtils.notEmpty(userMessageId, "用户消息主键不能为空");

        // 查询当前会话已有的最大轮次序号
        FindOneChatTurnRequest maxTurnRequest = new FindOneChatTurnRequest();
        maxTurnRequest.setSessionId(sessionId);
        ChatTurn maxTurn = chatTurnView.findOne(maxTurnRequest);
        int nextTurnNumber = maxTurn == null ? 1 : (maxTurn.getTurnNumber() == null ? 1 : maxTurn.getTurnNumber() + 1);

        // 创建新的对话轮次
        ChatTurn turn = new ChatTurn();
        turn.setSessionId(sessionId);
        turn.setTurnNumber(nextTurnNumber);
        turn.setUserMessageId(userMessageId);
        turn.setTaskId(taskId);
        turn.setStatus(Status.ON);
        chatTurnView.save(turn);
        return turn;
    }

    @Override
    public void completeTurn(String turnId, String assistantMessageId, String reasoningSummary) {
        AssertUtils.notEmpty(turnId, "轮次主键不能为空");

        // 查询轮次并更新AI回复消息ID和推理摘要
        ChatTurn turn = chatTurnView.findById(turnId);
        AssertUtils.notEmpty(turn, "轮次[{}]不存在", turnId);
        turn.setAssistantMessageId(assistantMessageId);

        // 受控推理摘要不存储模型原始思维链
        if (reasoningSummary != null && !reasoningSummary.isBlank()) {
            turn.setReasoningSummary(reasoningSummary);
        }
        chatTurnView.updateById(turn);
    }

    @Override
    public List<AgentChatMessage> loadRecentMessages(String sessionId, int maxTokens, int fullTurnCount) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");

        // 加载会话全部消息，按序号升序排列
        List<AgentChatMessage> allMessages = agentChatMessageView.findAllBySessionId(sessionId);
        if (allMessages.isEmpty()) {
            return new ArrayList<>();
        }

        // 从最新消息向前累积，按token上限裁剪
        List<AgentChatMessage> window = new ArrayList<>();
        int estimatedTokens = 0;
        int turnCount = 0;

        // 从最新到最旧遍历消息
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            AgentChatMessage msg = allMessages.get(i);
            int msgTokens = estimateTokens(msg.getContent());

            // 超过上限时停止累积
            if (estimatedTokens + msgTokens > maxTokens) {
                break;
            }

            // 插入到窗口头部以保持时间顺序
            window.add(0, msg);
            estimatedTokens += msgTokens;

            // 仅保留最近N轮完整内容，更早轮次仅保留摘要标记
            if (msg.getTurnId() != null && !msg.getTurnId().isBlank()) {
                turnCount++;
            }
        }

        return window;
    }

    /**
     * 估算文本的token数量。
     * <p>使用字符数/2.5的中英文混合近似算法。</p>
     *
     * @param text 文本内容
     * @return 估算的token数量
     */
    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 2.5);
    }
}

