package com.unionclass.chatting.application.port.out;

import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import com.unionclass.chatting.application.port.dto.ChatRoomGetDto;
import com.unionclass.chatting.application.port.dto.ChatRoomSaveDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatServiceRepositoryPort {

    Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto);

    Flux<ChatRoomGetDto> getChatRooms();

    Mono<ChatRoomGetDto> getChatRoomByUuid(String chatRoomUuid);

    Mono<ChatRoomGetDto> saveChatRoom(ChatRoomSaveDto chatRoomSaveDto);
}
