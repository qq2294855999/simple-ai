package com.simple.ai.view.chatTurn;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.chatTurn.DeleteChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.FindAllChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.FindOneChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.PageChatTurnRequest;
import com.simple.ai.common.entity.chatTurn.ChatTurn;
import com.simple.ai.common.view.chatTurn.ChatTurnView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对话轮次(chat_turn)数据库视图实现
 *
 * @author qty
 */
@Component
class MPChatTurnView implements ChatTurnView {

    @Autowired
    private ChatTurnRepository repository;

    @Override
    public IPage<ChatTurn> findAll(PageChatTurnRequest pageRequest) {
        LambdaQueryWrapper<ChatTurn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getSessionId()), ChatTurn::getSessionId, pageRequest.getSessionId())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getTurnNumber()), ChatTurn::getTurnNumber, pageRequest.getTurnNumber())
                    .like(ObjUtil.isNotEmpty(pageRequest.getUserMessageId()), ChatTurn::getUserMessageId, pageRequest.getUserMessageId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, pageRequest.getAssistantMessageId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTaskId()), ChatTurn::getTaskId, pageRequest.getTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, pageRequest.getReasoningSummary())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStatus()), ChatTurn::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), ChatTurn::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(ChatTurn.class), queryWrapper);
    }

    @Override
    public List<ChatTurn> findAll(FindAllChatTurnRequest findAllRequest, FindAllChatTurnRequest neRequest) {
        LambdaQueryWrapper<ChatTurn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), ChatTurn::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSessionId()), ChatTurn::getSessionId, findAllRequest.getSessionId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTurnNumber()), ChatTurn::getTurnNumber, findAllRequest.getTurnNumber())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getUserMessageId()), ChatTurn::getUserMessageId, findAllRequest.getUserMessageId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, findAllRequest.getAssistantMessageId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskId()), ChatTurn::getTaskId, findAllRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, findAllRequest.getReasoningSummary())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), ChatTurn::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), ChatTurn::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ChatTurn::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSessionId()), ChatTurn::getSessionId, neRequest.getSessionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnNumber()), ChatTurn::getTurnNumber, neRequest.getTurnNumber())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUserMessageId()), ChatTurn::getUserMessageId, neRequest.getUserMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, neRequest.getAssistantMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ChatTurn::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, neRequest.getReasoningSummary())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ChatTurn::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), ChatTurn::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public ChatTurn findOne(FindOneChatTurnRequest findOneRequest, FindOneChatTurnRequest neRequest) {
        LambdaQueryWrapper<ChatTurn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), ChatTurn::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSessionId()), ChatTurn::getSessionId, findOneRequest.getSessionId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnNumber()), ChatTurn::getTurnNumber, findOneRequest.getTurnNumber())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getUserMessageId()), ChatTurn::getUserMessageId, findOneRequest.getUserMessageId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, findOneRequest.getAssistantMessageId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), ChatTurn::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, findOneRequest.getReasoningSummary())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), ChatTurn::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), ChatTurn::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ChatTurn::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSessionId()), ChatTurn::getSessionId, neRequest.getSessionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnNumber()), ChatTurn::getTurnNumber, neRequest.getTurnNumber())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUserMessageId()), ChatTurn::getUserMessageId, neRequest.getUserMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, neRequest.getAssistantMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ChatTurn::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, neRequest.getReasoningSummary())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ChatTurn::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), ChatTurn::getRemark, neRequest.getRemark());

        List<ChatTurn> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneChatTurnRequest findOneRequest, FindOneChatTurnRequest neRequest) {
        LambdaQueryWrapper<ChatTurn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), ChatTurn::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSessionId()), ChatTurn::getSessionId, findOneRequest.getSessionId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnNumber()), ChatTurn::getTurnNumber, findOneRequest.getTurnNumber())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getUserMessageId()), ChatTurn::getUserMessageId, findOneRequest.getUserMessageId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, findOneRequest.getAssistantMessageId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), ChatTurn::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, findOneRequest.getReasoningSummary())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), ChatTurn::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), ChatTurn::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ChatTurn::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSessionId()), ChatTurn::getSessionId, neRequest.getSessionId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnNumber()), ChatTurn::getTurnNumber, neRequest.getTurnNumber())
                    .ne(ObjUtil.isNotEmpty(neRequest.getUserMessageId()), ChatTurn::getUserMessageId, neRequest.getUserMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAssistantMessageId()), ChatTurn::getAssistantMessageId, neRequest.getAssistantMessageId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ChatTurn::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReasoningSummary()), ChatTurn::getReasoningSummary, neRequest.getReasoningSummary())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ChatTurn::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), ChatTurn::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public ChatTurn findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(ChatTurn chatTurn) {
        repository.insert(chatTurn);
    }

    @Override
    public void updateById(ChatTurn chatTurn) {
        repository.updateById(chatTurn);
    }

    @Override
    public void updateById(List<ChatTurn> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<ChatTurn> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteChatTurnRequest request) {
        LambdaQueryWrapper<ChatTurn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), ChatTurn::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getSessionId()), ChatTurn::getSessionId, request.getSessionId())
                    .eq(ObjUtil.isNotEmpty(request.getTurnNumber()), ChatTurn::getTurnNumber, request.getTurnNumber())
                    .eq(ObjUtil.isNotEmpty(request.getUserMessageId()), ChatTurn::getUserMessageId, request.getUserMessageId())
                    .eq(ObjUtil.isNotEmpty(request.getAssistantMessageId()), ChatTurn::getAssistantMessageId, request.getAssistantMessageId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskId()), ChatTurn::getTaskId, request.getTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getReasoningSummary()), ChatTurn::getReasoningSummary, request.getReasoningSummary())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), ChatTurn::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), ChatTurn::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

