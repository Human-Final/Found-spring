package com.human.found.domain.notice.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class NoticeVO {
    private Long num;               // 공지사항 번호 (PK)
    private String id;              // 관리자 ID (작성자, FK)
    private String title;           // 제목
    private String content;         // 내용
    private Long viewCount;         // 조회수
    private Integer isPlanned;      // 팝업 여부 (0: 일반, 1: 팝업)
    private Integer isImportant;    // 주요 공지 여부 (0: 일반, 1: 상단고정)
    private LocalDateTime createdAt;// 등록일시
    private LocalDateTime updatedAt;// 수정일시
    private Integer isDeleted;      // 삭제여부 (0: 유지, 1: 삭제)
    
    // 파일 업로드를 위한 추가 필드
    private String imagePath;       // DB 보관용 이미지 경로 (요구사항 반영)
    private MultipartFile uploadFile; // 관리자가 첨부할 파일 1개
}
