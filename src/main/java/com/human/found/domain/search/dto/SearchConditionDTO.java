package com.human.found.domain.search.dto;

import com.human.found.global.common.paging.PagingVO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchConditionDTO extends PagingVO {
    
    private String keyword;
    private String boardType = "all";       // all, lost, found
    private String status = "all";          // all, progress, done
    private String category;    
    private String color;       
    private String place;
    
    private String eventDate;   
    private String startDate;
    private String endDate;
    
    private String searchMode = "like";
}
