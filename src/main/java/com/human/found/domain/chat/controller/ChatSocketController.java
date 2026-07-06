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

        // DB 저장
        chatService.saveMessage(message);
        
        // 메시지를 구독하고 있는 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getChatNum(), message);
    }
}
 