package com.unionclass.chatting.adaptor.in.web.controller;

import com.unionclass.chatting.adaptor.in.web.mapper.ChatMessageMapper;
import com.unionclass.chatting.adaptor.in.web.mapper.ChatRoomMapper;
import com.unionclass.chatting.adaptor.in.web.vo.ChatMessageRequestVo;
import com.unionclass.chatting.adaptor.in.web.vo.ChatRoomRequestVo;
import com.unionclass.chatting.adaptor.in.web.vo.ChatRoomResponseVo;
import com.unionclass.chatting.application.port.in.ChatServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
@CrossOrigin(origins = "*")
public class ChatServiceRestController {

    private final ChatServiceUseCase chatServiceUseCase;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatRoomMapper chatRoomMapper;

    @PostMapping("/send")
    public Mono<Void> sendChatMessage(
            @RequestBody ChatMessageRequestVo chatMessageRequestVo
    ) {
        return chatServiceUseCase.sendChatMessage(chatMessageMapper.toDto(
                chatMessageRequestVo
        ));
    }

    @GetMapping("/rooms")
    public Flux<ChatRoomResponseVo> getChatRooms() {
        return chatRoomMapper.toFluxVo(chatServiceUseCase.getChatRooms());
    }

    @GetMapping("/rooms/{chatRoomUuid}")
    public Mono<ChatRoomResponseVo> getChatRoom(@PathVariable String chatRoomUuid) {
        return chatServiceUseCase.getChatRoomByUuid(chatRoomUuid)
                .map(chatRoomMapper::toVo)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "채팅방을 찾을 수 없습니다."
                )));
    }

    @PostMapping("/rooms")
    public Mono<ChatRoomResponseVo> createChatRoom(
            @RequestBody ChatRoomRequestVo chatRoomRequestVo
    ) {
        return chatServiceUseCase.createChatRoom(chatRoomMapper.toSaveDto(chatRoomRequestVo))
                .map(chatRoomMapper::toVo);
    }
}
