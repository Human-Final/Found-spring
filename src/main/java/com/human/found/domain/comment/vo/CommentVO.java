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

    //추가할 속성: 습득물/분실물 구분용 필드
    private String boardType; 
    //추가 : 글 제목 postTitle로 저장
    private String postTitle;
    //추가 : 댓글의 주소 게시글의 atcId를 저장
    private String atcId;

}
