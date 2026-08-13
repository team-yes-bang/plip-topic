package com.unionclass.chatting.application.port.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageResponseDto {

    private String chatMessageUuid;
    private String chatRoomUuid;
    private String messageType;
    private String message;
    private String senderUuid;
    private String createdAt;
    private String updatedAt;

    @Builder
    public ChatMessageResponseDto(String chatMessageUuid, String chatRoomUuid, String messageType, String message, String senderUuid, String createdAt, String updatedAt) {
        this.chatMessageUuid = chatMessageUuid;
        this.chatRoomUuid = chatRoomUuid;
        this.messageType = messageType;
        this.message = message;
        this.senderUuid = senderUuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}