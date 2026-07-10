package com.human.found.domain.user.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyPagePostVO {
    private String boardType;   // 습득 또는 분실
    private Long num;           // 글 번호
    private String title;       // 물품명 (fd_prdt_nm, lst_prdt_nm)
    private String content;     // 내용(fd_sbjt, lst_sbjt)
    private String category;    // 카테고리 (prdt_cl_nm)
    private Integer done;       // 진행 상태 (0 또는 1)
    private LocalDateTime createdAt; // 게시글 실제 등록 일자
    private String atcId;
}
