package com.human.found.domain.notice.vo;

import lombok.Data;

@Data
public class NoticeFileVO {
    private Long id;                // 이미지 번호 (PK)
    private String fdFilepathImg;   // 이미지 이름
    private Long num;               // 공지사항 번호 (FK)
    private String saveName;        // 랜덤 이름
    private Long fileSize;          // 파일 크기
    private String filePath;        // 파일 경로
}
