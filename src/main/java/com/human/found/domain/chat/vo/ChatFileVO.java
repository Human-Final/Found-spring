package com.human.found.domain.chat.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatFileVO {

    private Long fileNum;
    private Long messageNum;
    private String originalName;
    private String saveName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime createdAt;

}
