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
    public String searchView(Model model, SearchConditionDTO searchConditionDTO) {
        
        List<SearchResultVO> searchList = searchService.totalSearch(searchConditionDTO);

        model.addAttribute("keyword", searchConditionDTO.getKeyword());
        model.addAttribute("boardType", searchConditionDTO.getBoardType());
        model.addAttribute("status", searchConditionDTO.getStatus());

        model.addAttribute("searchList", searchList);
        model.addAttribute("paging", searchConditionDTO);

        return "search/result";
    }

    // 검색 결과를 json형태로 반환
    // @GetMapping("/api/search")
    // @ResponseBody
    // public List<SearchResultVO> search(SearchConditionDTO searchConditionDTO) {
    //     return searchService.totalSearch(searchConditionDTO);
    // }
}
