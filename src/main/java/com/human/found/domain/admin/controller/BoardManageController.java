package com.human.found.domain.admin.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.BoardManageService;
import com.human.found.domain.admin.vo.AdminSearchVO;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.lost.vo.LostVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.io.IOException;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BoardManageController {

    private final BoardManageService boardManageService;

    /**
     * 관리자 분실물 게시글 목록
     * - 검색 조건에 따라 사용자 등록 / 외부 연동 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/lost")
    public String lostList(AdminSearchVO searchVO, Model model) {
        if (boardManageService.isSearchConditionEmpty(searchVO)) {
            searchVO.pageInfo(0);
            model.addAttribute("lostList", Collections.emptyList());
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "lost");
        } else {
            int totalCount = boardManageService.countSearchLost(searchVO);
            searchVO.setSize(50);
            searchVO.pageInfo(totalCount);

            model.addAttribute("lostList", boardManageService.searchLostPage(searchVO));
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
            boardManageService.deleteLostList(atcIds);
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
            boardManageService.completeLostList(atcIds);
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

        if (boardManageService.isSearchConditionEmpty(searchVO)) {
            // 모든 조건이 비어있다면 최초 진입 상태 -> 결과 0건 처리
            searchVO.pageInfo(0);
            model.addAttribute("foundList", Collections.emptyList());
            model.addAttribute("searchVO", searchVO);
            model.addAttribute("boardType", "found");
        } else {
            // 조건이 하나라도 채워져 있다면 사용자가 검색을 시도한 것 -> 정상 DB 조회
            int totalCount = boardManageService.countSearchFound(searchVO);
            searchVO.setSize(50);
            searchVO.pageInfo(totalCount);

            model.addAttribute("foundList", boardManageService.searchFoundPage(searchVO));
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
            boardManageService.deleteFoundList(atcIds);
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
            boardManageService.completeFoundList(atcIds);
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        
        return "redirect:/admin/found";
    }

    // 관리자 분실물 삭제된 게시글 미리보기

    @GetMapping("lost/preview/{atcId}")
    public String previewlost(@PathVariable String atcId,Model model) {
        model.addAttribute("lostVO", boardManageService.adminLostDetail(atcId));
        model.addAttribute("boardType", "lost");
        
        return "admin/boardPreview";
    }

    // 관리자 습득물 삭제된 게시글 미리보기

    @GetMapping("/found/preview/{atcId}")
    public String previewfound(@PathVariable String atcId,Model model) {

        model.addAttribute("foundVO", boardManageService.adminFoundDetail(atcId));
        model.addAttribute("boardType", "found");


        return "admin/boardPreview";
    }
    
    // 1. 분실물 전용 다운로드 API (기존 LostVO 그대로 연동)
    @GetMapping("/lost/download")
    public void downloadLostExcel(AdminSearchVO searchVO, HttpServletResponse response) throws IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"lost_post_list.xlsx\"");

        // 인터페이스 규격에 맞춰 AdminSearchVO 전달
        boardManageService.generateLostExcel(searchVO, response.getOutputStream());
    }


    // 2. 습득물 전용 다운로드 API (기존 FoundVO 그대로 연동)
    @GetMapping("/found/download")
    public void downloadFoundExcel(AdminSearchVO searchVO, HttpServletResponse response) throws IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"found_post_list.xlsx\"");

        // 인터페이스 규격에 맞춰 AdminSearchVO 전달
        boardManageService.generateFoundExcel(searchVO, response.getOutputStream());
    }

    



}
