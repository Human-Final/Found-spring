package com.human.found.domain.search.dto;

import com.human.found.global.common.paging.PagingVO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchConditionDTO extends PagingVO {
    private String keyword;

    private String boardType;
    private String status;

}
