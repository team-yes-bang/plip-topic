package com.unionclass.chatting.application.port.out;

import com.unionclass.chatting.application.port.dto.ChatMessageGetDto;
import reactor.core.publisher.Flux;

public interface ChatServiceReactiveRepositoryPort {

    Flux<ChatMessageGetDto> getChatByChatRoomUuid(String chatRoomUuid);
    Flux<ChatMessageGetDto> getLatestChatByChatRoomUuid(String chatRoomUuid);

}
