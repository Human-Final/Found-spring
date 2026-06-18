package com.human.found.domain.found.vo;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FoundVO {
    
    private Long num;

    private String atcId;
    private String id;
    private String clrNm;
    private String depPlace;
    private String fdFilepathImg;
    @NotBlank(message = "물품명은 필수 입력 사항입니다.")
    private String fdPrdtNm;
    @NotBlank(message = "게시글 제목은 필수 입력 사항입니다.")
    private String fdSbjt;
    // @NotBlank(message = "습득일자는 필수 입력 사항입니다.")
    private LocalDateTime fdYmd;
    private String prdtClNm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer done = 0;
    private Integer isDeleted = 0;
    
}
