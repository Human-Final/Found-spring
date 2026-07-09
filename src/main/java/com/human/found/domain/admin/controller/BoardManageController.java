package com.human.found.domain.admin.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.BoardManageService;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BoardManageController {

    private final BoardManageService boardManagerService;

    /**
     * 관리자 분실물 게시글 목록
     * - 검색 조건에 따라 사용자 등록 / 외부 연동 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/lost")
    public String lostList(AdminSearchVO searchVO, Model model) {
        if (boardManagerService.isSearchConditionEmpty(searchVO)) {
            searchVO.pageInfo(0);
            model.addAttribute("lostList", Collections.emptyList());
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "lost");
        } else {
            int totalCount = boardManagerService.countSearchLost(searchVO);
            searchVO.setSize(50);
            searchVO.pageInfo(totalCount);

            model.addAttribute("lostList", boardManagerService.searchLostPage(searchVO));
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "lost");
        }

        return "admin/boardManage";
    }

    /**
     * 관리자 분실물 선택 삭제
     * - dataSource 값에 따라 lost / lost_police 테이블 논리 삭제
     */
    @PostMapping("/lost/delete")
    public String deleteLost(
            @RequestParam("atcId") List<String> atcIds, 
            @RequestHeader(value = "Referer", required = false) String referer) {
        
        if (atcIds != null && !atcIds.isEmpty()) {
            boardManagerService.deleteLostList(atcIds);
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/admin/lost";
    }

    // 관리자 분실물 게시글 완료처리
    @PostMapping("/lost/complete")
    public String completeLost(
            @RequestParam("atcId") List<String> atcIds,
            @RequestHeader(value = "Referer", required = false) String referer) {
        
                if (atcIds != null && !atcIds.isEmpty()) {
            boardManagerService.completeLostList(atcIds);
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        
        return "redirect:/admin/lost";
    }

    /**
     * 관리자 습득물 게시글 목록
     * - 검색 조건에 따라 사용자 등록 / 외부 연동 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/found")
    public String foundList(AdminSearchVO searchVO, Model model) {

        if (boardManagerService.isSearchConditionEmpty(searchVO)) {
            // 모든 조건이 비어있다면 최초 진입 상태 -> 결과 0건 처리
            searchVO.pageInfo(0);
            model.addAttribute("foundList", Collections.emptyList());
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "found");
        } else {
            // 조건이 하나라도 채워져 있다면 사용자가 검색을 시도한 것 -> 정상 DB 조회
            int totalCount = boardManagerService.countSearchFound(searchVO);
            searchVO.setSize(50);
            searchVO.pageInfo(totalCount);

            model.addAttribute("foundList", boardManagerService.searchFoundPage(searchVO));
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "found");
        }

        return "admin/boardManage";
    }

    /**
     * 관리자 습득물 선택 삭제
     * - dataSource 값에 따라 found / found_police 테이블 논리 삭제
     */
    @PostMapping("/found/delete")
    public String deleteFound(
            @RequestParam("atcId") List<String> atcIds, 
            @RequestHeader(value = "Referer", required = false) String referer) {
        
        if (atcIds != null && !atcIds.isEmpty()) {
            boardManagerService.deleteFoundList(atcIds);
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/admin/found";
    }

    // 관리자 분실물 게시글 완료처리
    @PostMapping("/found/complete")
    public String completeFound(
            @RequestParam("atcId") List<String> atcIds,
            @RequestHeader(value = "Referer", required = false) String referer) {
        
                if (atcIds != null && !atcIds.isEmpty()) {
            boardManagerService.completeFoundList(atcIds);
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        
        return "redirect:/admin/found";
    }
}
