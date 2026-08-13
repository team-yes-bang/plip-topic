package com.unionclass.chatting.adaptor.out.mongodb.mapper;

import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.unionclass.chatting.application.port.dto.ChatMessageGetDto;
import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import com.unionclass.chatting.application.port.dto.ChatRoomGetDto;
import com.unionclass.chatting.application.port.dto.ChatRoomSaveDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
public class ChatEntityMapper {

    public Flux<ChatMessageGetDto> chatMessageGetDtoFlux(Flux<ChatMessageEntity> chatMessageEntityFlux) {
        return chatMessageEntityFlux.map(chatMessageEntity ->
                ChatMessageGetDto.builder()
                        .chatMessageUuid(chatMessageEntity.getId())
                        .chatRoomUuid(chatMessageEntity.getChatRoomUuid())
                        .message(chatMessageEntity.getMessage())
                        .messageType(chatMessageEntity.getMessageType())
                        .senderUuid(chatMessageEntity.getSenderUuid())
                        .createdAt(chatMessageEntity.getCreatedAt() != null ? chatMessageEntity.getCreatedAt().toString() : null)
                        .updatedAt(chatMessageEntity.getUpdatedAt() != null ? chatMessageEntity.getUpdatedAt().toString() : null)
                        .build()
                );
    }


    public ChatMessageEntity toEntity(ChatMessageSaveDto chatMessageSaveDto) {
        Instant now = Instant.now();
        return ChatMessageEntity.builder()
                .chatRoomUuid(chatMessageSaveDto.getChatRoomUuid())
                .messageType(chatMessageSaveDto.getMessageType())
                .message(chatMessageSaveDto.getMessage())
                .senderUuid(chatMessageSaveDto.getSenderUuid())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public ChatRoomEntity toChatRoomEntity(ChatRoomSaveDto chatRoomSaveDto) {
        Instant now = Instant.now();
        String chatRoomUuid = chatRoomSaveDto.getChatRoomUuid() != null && !chatRoomSaveDto.getChatRoomUuid().isBlank()
                ? chatRoomSaveDto.getChatRoomUuid()
                : UUID.randomUUID().toString();

        return ChatRoomEntity.builder()
                .chatRoomUuid(chatRoomUuid)
                .roomName(chatRoomSaveDto.getRoomName())
                .participantEntityList(Collections.emptyList())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public ChatRoomGetDto toChatRoomGetDto(ChatRoomEntity entity) {
        return toChatRoomGetDto(entity, null);
    }

    public ChatRoomGetDto toChatRoomGetDto(ChatRoomEntity entity, ChatMessageEntity lastMessage) {
        return ChatRoomGetDto.builder()
                .chatRoomUuid(entity.getChatRoomUuid())
                .roomName(entity.getRoomName())
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .lastMessageAt(lastMessage != null && lastMessage.getCreatedAt() != null
                        ? lastMessage.getCreatedAt().toString()
                        : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)
                .build();
    }

}
