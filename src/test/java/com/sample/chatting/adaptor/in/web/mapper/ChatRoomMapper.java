package com.unionclass.chatting.adaptor.in.web.mapper;

import com.unionclass.chatting.adaptor.in.web.vo.ChatRoomRequestVo;
import com.unionclass.chatting.adaptor.in.web.vo.ChatRoomResponseVo;
import com.unionclass.chatting.application.port.dto.ChatRoomResponseDto;
import com.unionclass.chatting.application.port.dto.ChatRoomSaveDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ChatRoomMapper {

    public Flux<ChatRoomResponseVo> toFluxVo(Flux<ChatRoomResponseDto> chatRoomResponseDtoFlux) {
        return chatRoomResponseDtoFlux.map(this::toVo);
    }

    public ChatRoomResponseVo toVo(ChatRoomResponseDto dto) {
        return ChatRoomResponseVo.builder()
                .chatRoomUuid(dto.getChatRoomUuid())
                .roomName(dto.getRoomName())
                .lastMessage(dto.getLastMessage())
                .lastMessageAt(dto.getLastMessageAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public ChatRoomSaveDto toSaveDto(ChatRoomRequestVo requestVo) {
        return ChatRoomSaveDto.builder()
                .chatRoomUuid(requestVo.getChatRoomUuid())
                .roomName(requestVo.getRoomName())
                .build();
    }
}
