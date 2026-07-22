package com.simple.ai.common.copy.chatTurn;

import com.simple.ai.common.dto.chatTurn.CreateChatTurnRequest;
import com.simple.ai.common.dto.chatTurn.InfoChatTurnResponse;
import com.simple.ai.common.dto.chatTurn.PageChatTurnResponse;
import com.simple.ai.common.dto.chatTurn.UpdateChatTurnRequest;
import com.simple.ai.common.entity.chatTurn.ChatTurn;
import org.mapstruct.Mapper;

/**
 * 对话轮次(chat_turn)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface ChatTurnCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageChatTurnResponse toPageResponse(ChatTurn entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoChatTurnResponse toInfoResponse(ChatTurn entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return ChatTurn 数据对象
     */
    ChatTurn toEntity(CreateChatTurnRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return ChatTurn 数据对象
     */
    ChatTurn toEntity(UpdateChatTurnRequest updateRequest);

}

