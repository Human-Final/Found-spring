package com.human.found.domain.comment.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentVO {
    
    private Long commentNum;
    private String id;
    private Long num;
    private String dataSource;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer isDeleted;
    private LocalDateTime deletedAt;
}
