package com.human.found.domain.found.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FoundVO {
    private Long num;
    private String atcId;
    private String id;
    private String clrNum;
    private String depPlace;
    private String fdFilepathImg;
    private String fdPrdtNum;
    private String fdSbjt;
    private LocalDateTime fdYmd;
    private String prdtClNum;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Integer done = 0;
    private Integer deleted = 0;

}
