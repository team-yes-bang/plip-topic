package com.unionclass.chatting.adaptor.in.web.vo;

import lombok.Getter;

@Getter
public class ChatMessageRequestVo {

    private String chatRoomUuid;
    private String messageType;
    private String message;
    private String senderUuid;

}
