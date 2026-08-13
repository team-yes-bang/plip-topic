package com.unionclass.chatting.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Participant {

    private String userUuid;
    private String nickName;
    private Integer unreadCount;

    @Builder
    public Participant(String userUuid, String nickName, Integer unreadCount) {
        this.userUuid = userUuid;
        this.nickName = nickName;
        this.unreadCount = unreadCount;
    }
}
