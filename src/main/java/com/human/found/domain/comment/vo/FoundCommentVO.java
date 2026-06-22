package com.human.found.domain.comment.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FoundCommentVO {
    
    private Long commentNum;
    private String id;
    private Long num;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer isDeleted;
    private LocalDateTime deletedAt;

    // 경찰청 API, 습득물 등 경로 알아내기 위함
    private String dataSource;
}

