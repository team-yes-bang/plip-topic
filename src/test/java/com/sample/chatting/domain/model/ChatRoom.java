package com.unionclass.chatting.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatRoom {

    private String chatRoomUuid;
    private String roomName;
    private List<Participant> participantList;
    private String createdAt;
    private String updatedAt;

    @Builder
    public ChatRoom(String chatRoomUuid, String roomName, List<Participant> participantList, String createdAt, String updatedAt) {
        this.chatRoomUuid = chatRoomUuid;
        this.roomName = roomName;
        this.participantList = participantList;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
