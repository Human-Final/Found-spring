package com.human.found.domain.search.service;

import java.util.List;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpServletRequest;

public interface SearchService {
    
    List<SearchResultVO> totalLikeSearch(SearchConditionDTO conditionDTO);
    List<SearchResultVO> hybridSearch(SearchConditionDTO conditionDTO, HttpServletRequest request);

}
