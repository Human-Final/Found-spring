package com.human.found.domain.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.AdminService;
import com.human.found.domain.admin.vo.AdminSearchVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 분실물 게시글 목록
     * - 검색 조건에 따라 사용자 등록 / 외부 연동 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/lost")
    public String lostList(AdminSearchVO searchVO, Model model) {
        searchVO.setBoardType("lost");
        searchVO.setSize(10);

        int totalCount = adminService.countSearchLost(searchVO);
        searchVO.pageInfo(totalCount);

        model.addAttribute("lostList", adminService.searchLostPage(searchVO));
        model.addAttribute("searchVO", searchVO);
        model.addAttribute("boardType", "lost");

        return "admin/adminPage";
    }

    /**
     * 관리자 분실물 선택 삭제
     * - dataSource 값에 따라 lost / lost_police 테이블 논리 삭제
     */
    @PostMapping("/lost/delete")
    public String deleteLostList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            @RequestParam(value = "nums", required = false) List<Long> nums) {

        if ("POLICE".equals(dataSource)) {
            adminService.deletePoliceLostList(nums);
            return "redirect:/admin/lost?dataSources=POLICE";
        }

        adminService.deleteLostList(nums);
        return "redirect:/admin/lost?dataSources=USER";
    }

    /**
     * 관리자 습득물 게시글 목록
     * - 검색 조건에 따라 사용자 등록 / 외부 연동 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/found")
    public String foundList(AdminSearchVO searchVO, Model model) {

        searchVO.setBoardType("found");
        searchVO.setSize(10);

        int totalCount = adminService.countSearchFound(searchVO);
        searchVO.pageInfo(totalCount);

        model.addAttribute("foundList", adminService.searchFoundPage(searchVO));
        model.addAttribute("searchVO", searchVO);
        model.addAttribute("boardType", "found");

        return "admin/adminPage";
    }

    /**
     * 관리자 습득물 선택 삭제
     * - dataSource 값에 따라 found / found_police 테이블 논리 삭제
     */
    @PostMapping("/found/delete")
    public String deleteFoundList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            @RequestParam(value = "nums", required = false) List<Long> nums) {

        if ("POLICE".equals(dataSource)) {
            adminService.deletePoliceFoundList(nums);
            return "redirect:/admin/found?dataSources=POLICE";
        }

        adminService.deleteFoundList(nums);
        return "redirect:/admin/found?dataSources=USER";
    }

    /**
     * 관리자 공지사항 목록
     * - 검색 조건에 따라 관리자 작성 공지사항 목록 조회
     * - PagingVO를 상속받은 AdminSearchVO를 이용하여 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/notice")
    public String noticeList(AdminSearchVO searchVO, Model model) {

        searchVO.setBoardType("notice");
        searchVO.setSize(10);

        int totalCount = adminService.countSearchNotice(searchVO);
        searchVO.pageInfo(totalCount);

        model.addAttribute("noticeList", adminService.searchNoticePage(searchVO));
        model.addAttribute("searchVO", searchVO);
        model.addAttribute("boardType", "notice");

        return "admin/adminPage";
    }

    /**
     * 관리자 공지사항 선택 삭제
     */
    @PostMapping("/notice/delete")
    public String deleteNoticeList(
            @RequestParam(value = "nums", required = false) List<Long> nums) {

        adminService.deleteNoticeList(nums);

        return "redirect:/admin/notice";
    }
}
