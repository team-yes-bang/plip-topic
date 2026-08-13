package com.unionclass.chatting.adaptor.in.reactive.mapper;

import com.unionclass.chatting.adaptor.in.reactive.vo.ChatMessageResponseVo;
import com.unionclass.chatting.application.port.dto.ChatMessageResponseDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ChatMessageFluxMapper {

    public Flux<ChatMessageResponseVo> toFluxVo(Flux<ChatMessageResponseDto> chatMessageResponseDtoFlux) {
        return chatMessageResponseDtoFlux.map(
                chatMessageResponseDto -> ChatMessageResponseVo.builder()
                        .chatMessageUuid(chatMessageResponseDto.getChatMessageUuid())
                        .message(chatMessageResponseDto.getMessage())
                        .messageType(chatMessageResponseDto.getMessageType())
                        .senderUuid(chatMessageResponseDto.getSenderUuid())
                        .chatRoomUuid(chatMessageResponseDto.getChatRoomUuid())
                        .createdAt(chatMessageResponseDto.getCreatedAt())
                        .updatedAt(chatMessageResponseDto.getUpdatedAt())
                        .build());
    }

}
