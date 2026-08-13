package com.unionclass.chatting.application.port.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatMessageRequestDto {

    private String chatRoomUuid;
    private String messageType;
    private String message;
    private String senderUuid;

    @Builder
    public ChatMessageRequestDto(String chatRoomUuid, String messageType, String message, String senderUuid) {
        this.chatRoomUuid = chatRoomUuid;
        this.messageType = messageType;
        this.message = message;
        this.senderUuid = senderUuid;
    }

}