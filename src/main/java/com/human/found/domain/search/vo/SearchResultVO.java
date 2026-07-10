package com.human.found.domain.search.vo;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResultVO {
    private String atcId;
    private String id;
    private String color;

    private String place;
    private String imagePath;
    private String category;

    private String content;
    private LocalDate eventDate;
    private String title;    

    private Integer done;

    private String dataSource;
    private String boardType;

    private String searchKey;

    private String matchType;   // LIKE, LLM, BOTH
    private int matchScore;     // 우선정렬 목적 BOTH > LLM / LIKE

}
