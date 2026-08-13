package com.unionclass.chatting.adaptor.in.reactive.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatMessageResponseVo {

    private String chatMessageUuid;
    private String chatRoomUuid;
    private String messageType;
    private String message;
    private String senderUuid;
    private String createdAt;
    private String updatedAt;

    @Builder

    public ChatMessageResponseVo(
            String chatMessageUuid,
            String chatRoomUuid,
            String messageType,
            String message,
            String senderUuid,
            String createdAt,
            String updatedAt
    ) {
        this.chatMessageUuid = chatMessageUuid;
        this.chatRoomUuid = chatRoomUuid;
        this.messageType = messageType;
        this.message = message;
        this.senderUuid = senderUuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
