package com.human.found.domain.search.dto;

import com.human.found.global.common.paging.PagingVO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchConditionDTO extends PagingVO {
    private String keyword;

    private String boardType;   // lost, found
    private String status;      // prograss, done
    private String category;    
    private String color;       
    private String place;
    private String dateText;   

}
