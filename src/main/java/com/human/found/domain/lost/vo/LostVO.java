package com.human.found.domain.lost.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LostVO {
    private Long num;
    private String atcId;
    private String id;
    private String lstPlace;
    private String lstPrdtNum;
    private String lstSbjt;
    private LocalDateTime lstYmd;
    private String prdtClNum;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Integer done = 0;
    private Integer deleted = 0;
}
