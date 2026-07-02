package com.human.found.domain.admin.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminLostVO {
    
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

    private Integer done;
    private Integer isDeleted;
    private String dataSource;

    private String lstFilepathImg;
    private Long viewConut;
}
