package com.unionclass.chatting.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatRoomResponseVo {

    private String chatRoomUuid;
    private String roomName;
    private String lastMessage;
    private String lastMessageAt;
    private String createdAt;
    private String updatedAt;

    @Builder
    public ChatRoomResponseVo(
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
