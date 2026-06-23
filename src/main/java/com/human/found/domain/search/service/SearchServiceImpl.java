package com.human.found.domain.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.mapper.SearchMapper;
import com.human.found.domain.search.vo.SearchResultVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private final SearchMapper searchMapper;

    @Override
    public List<SearchResultVO> totalSearch(SearchConditionDTO searchConditionDTO) {
       
        int totalCount = searchMapper.countTotalSearch(searchConditionDTO);
        searchConditionDTO.pageInfo(totalCount);

        return searchMapper.totalSearch(searchConditionDTO);
    }

}
