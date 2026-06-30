package com.human.found.domain.admin.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminNoticeVO {
    
    private Long num;
    private String id;

    private String title;
    private String content;

    private Long viewCount;
    private Integer isPlanned;
    private Integer isImportant;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer isDeleted;
}
