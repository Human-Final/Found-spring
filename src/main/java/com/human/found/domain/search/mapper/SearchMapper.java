package com.human.found.domain.search.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.vo.SearchResultVO;

@Mapper
public interface SearchMapper {
    List<SearchResultVO> totalSearch(SearchConditionDTO searchConditionDTO);
    int countTotalSearch(SearchConditionDTO searchConditionDTO);
}
