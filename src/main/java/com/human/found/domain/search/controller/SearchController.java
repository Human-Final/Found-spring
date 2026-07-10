package com.human.found.domain.search.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.service.SearchService;
import com.human.found.domain.search.vo.SearchResultVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 사용자가 보는 view
    @GetMapping("/search")
    public String searchView(Model model, SearchConditionDTO conditionDTO) {
        
        List<SearchResultVO> searchList;
        
        // LLM/LIKE 분기
        // 나중에는 searchList = searchService.hybridSearch(conditionDTO); 만 남기면 됨
        // LIKE 기반 검색 확인용 URL 예시 : /search?keyword=핸드폰&searchMode=like
        // 헤더쪽 검색은 아직 LIKE 기반입니다.
        if("hybrid".equals(conditionDTO.getSearchMode())){
            searchList = searchService.hybridSearch(conditionDTO);
        }else {
            searchList = searchService.totalLikeSearch(conditionDTO);
        }

        // 검색 키워드
        model.addAttribute("conditionDTO", conditionDTO);
        
        // 검색 결과
        model.addAttribute("searchList", searchList);
        
        // 페이징
        model.addAttribute("paging", conditionDTO);

        return "search/result";
    }

    // 검색 결과를 json형태로 반환
    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchResultVO> search(SearchConditionDTO conditionDTO) {
        return searchService.totalLikeSearch(conditionDTO);
    }
}
