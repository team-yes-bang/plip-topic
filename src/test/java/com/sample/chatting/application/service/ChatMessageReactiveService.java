package com.unionclass.chatting.application.service;

import com.unionclass.chatting.application.mapper.ChatServiceMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageResponseDto;
import com.unionclass.chatting.application.port.in.ChatServiceReactiveUseCase;
import com.unionclass.chatting.application.port.out.ChatServiceReactiveRepositoryPort;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import com.unionclass.chatting.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ChatMessageReactiveService implements ChatServiceReactiveUseCase {

    private final ChatServiceReactiveRepositoryPort chatServiceReactiveRepositoryPort;
    private final ChatServiceMapper chatServiceMapper;

    @Override
    public Flux<ChatMessageResponseDto> getChatByChatRoomUuid(String chatRoomUuid) {
        Flux<ChatMessage> getChat = chatServiceMapper.toChatMessage(
                chatServiceReactiveRepositoryPort.getChatByChatRoomUuid(chatRoomUuid));
        return chatServiceMapper.toChatMessageResponseDto(getChat);
    }

    @Override
    public Flux<ChatMessageResponseDto> getLatestChatByChatRoomUuid(String chatRoomUuid) {
        Flux<ChatMessage> getLatestChat = chatServiceMapper.toChatMessage(
                chatServiceReactiveRepositoryPort.getLatestChatByChatRoomUuid(chatRoomUuid)
        );
        return chatServiceMapper.toChatMessageResponseDto(getLatestChat);
    }


}
