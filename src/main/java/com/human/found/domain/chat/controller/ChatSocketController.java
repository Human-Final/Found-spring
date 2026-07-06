package com.human.found.domain.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.human.found.domain.chat.service.ChatService;
import com.human.found.domain.chat.vo.ChatMessageVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageVO message) {

        // 일반 텍스트 메시지만 DB 저장
        // 파일 메시지는 /chat/file/upload 에서 이미 저장됨
        if (message.getMessageNum() == null) {
            chatService.saveMessage(message);
        }

        // 구독 중인 클라이언트에게 전송
        messagingTemplate.convertAndSend(
            "/sub/chat/room/" + message.getChatNum(),
            message
        );
    }
}