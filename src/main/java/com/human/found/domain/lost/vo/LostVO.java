package com.human.found.domain.lost.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LostVO {
    private Long num;
    private String atcId;
    private String id;
    private String lstPlace;
    private String lstPrdtNm;
    private String lstSbjt;
    private LocalDateTime lstYmd;
    private String prdtClNm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer done = 0;
    private Integer isDeleted = 0;
}
