package com.unionclass.chatting.adaptor.in.reactive.controller;

import com.unionclass.chatting.adaptor.in.reactive.mapper.ChatMessageFluxMapper;
import com.unionclass.chatting.adaptor.in.reactive.vo.ChatMessageResponseVo;
import com.unionclass.chatting.application.port.in.ChatServiceReactiveUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/reactive")
@RestController
@CrossOrigin(origins = "*")
public class ChatMessageReactiveController {

    private final ChatServiceReactiveUseCase chatServiceReactiveUseCase;
    private final ChatMessageFluxMapper chatMessageFluxMapper;

    @GetMapping(value = "/{chatRoomUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessageResponseVo> getChatByChatRoomUuid(@PathVariable String chatRoomUuid) {
        return chatMessageFluxMapper.toFluxVo(chatServiceReactiveUseCase.getChatByChatRoomUuid(chatRoomUuid));
    }

    @GetMapping(value = "/{chatRoomUuid}/latest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessageResponseVo> getLatestChatByChatRoomUuid(@PathVariable String chatRoomUuid) {
        return chatMessageFluxMapper.toFluxVo(chatServiceReactiveUseCase.getLatestChatByChatRoomUuid(chatRoomUuid));
    }

}
