package com.human.found.domain.chat.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatRoomVO {

    private Long chatNum;

    private Long lostNum;
    private Long foundNum;

    private String userIdA;
    private String userIdB;

    private LocalDateTime createdAt;
}