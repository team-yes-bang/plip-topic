package com.unionclass.chatting.application.port.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomResponseDto {

    private String chatRoomUuid;
    private String roomName;
    private String lastMessage;
    private String lastMessageAt;
    private String createdAt;
    private String updatedAt;

    @Builder
    public ChatRoomResponseDto(
            String chatRoomUuid,
            String roomName,
            String lastMessage,
            String lastMessageAt,
            String createdAt,
            String updatedAt
    ) {
        this.chatRoomUuid = chatRoomUuid;
        this.roomName = roomName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
