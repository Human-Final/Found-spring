package com.human.found.domain.search.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.vo.SearchResultVO;

@Mapper
public interface SearchMapper {

    // 페이징용 전체 수 조회
    int countTotalSearch(SearchConditionDTO conditionDTO);

    // 페이징용 LIKE 기반 검색
    List<SearchResultVO> totalLikeSearch(SearchConditionDTO conditionDTO);

    // 하이브리드 검색 수집용 LIKE 검색
    List<SearchResultVO> candidateLikeSearch(SearchConditionDTO conditionDTO);

    // LLM 기반 검색
    List<SearchResultVO> llmSearch(SearchConditionDTO conditionDTO);
}
