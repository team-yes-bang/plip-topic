package com.unionclass.chatting.application.port.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomSaveDto {

    private String chatRoomUuid;
    private String roomName;

    @Builder
    public ChatRoomSaveDto(String chatRoomUuid, String roomName) {
        this.chatRoomUuid = chatRoomUuid;
        this.roomName = roomName;
    }
}
