package com.human.found.domain.found.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FoundVO {
    
    private Long num;

    private String atcId;
    private String id;

    private String clrNm;
    private String depPlace;
    private String fdFilepathImg;
    private String fdPrdtNm;
    private String fdSbjt;

    private LocalDateTime fdYmd;

    private String prdtClNm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer done;
    private Integer isDeleted;
}
