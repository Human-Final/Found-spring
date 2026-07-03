package com.human.found.domain.chat.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatMessageVO {

    private Long messageNum;

    private Long chatNum;
    private String senderId;

    private String content;
    private LocalDateTime createdAt;
}