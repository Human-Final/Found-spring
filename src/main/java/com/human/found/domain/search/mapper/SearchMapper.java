package com.human.found.domain.search.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.vo.SearchResultVO;

@Mapper
public interface SearchMapper {

    // 페이징용
    int countTotalSearch(SearchConditionDTO conditionDTO);

    // LIKE 기반 검색
    List<SearchResultVO> totalLikeSearch(SearchConditionDTO conditionDTO);

    // LLM 기반 검색
    List<SearchResultVO> llmSearch(SearchConditionDTO conditionDTO);
}
