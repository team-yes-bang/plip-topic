package com.unionclass.chatting.adaptor.in.web.mapper;

import com.unionclass.chatting.adaptor.in.web.vo.ChatMessageRequestVo;
import com.unionclass.chatting.application.port.dto.ChatMessageRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageRequestDto toDto(ChatMessageRequestVo chatMessageRequestVo) {
        return ChatMessageRequestDto.builder()
                .chatRoomUuid(chatMessageRequestVo.getChatRoomUuid())
                .message(chatMessageRequestVo.getMessage())
                .messageType(chatMessageRequestVo.getMessageType())
                .senderUuid(chatMessageRequestVo.getSenderUuid())
                .build();
    }

}
