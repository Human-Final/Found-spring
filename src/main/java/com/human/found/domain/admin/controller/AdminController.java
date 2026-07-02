package com.human.found.domain.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.AdminService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 분실물 게시글 목록
     * - dataSource 값에 따라 사용자 등록 / 외부 연동 목록 조회
     * - 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/lost")
    public String lostList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        int size = 10;
        int blockSize = 5;
        int totalCount;

        if ("POLICE".equals(dataSource)) {
            model.addAttribute("lostList", adminService.getPoliceLostPage(page, size));
            totalCount = adminService.countPoliceLost();
        } else {
            model.addAttribute("lostList", adminService.getLostPage(page, size));
            totalCount = adminService.countLost();
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startPage = ((page - 1) / blockSize) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPages);

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("boardType", "lost");

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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
            return "redirect:/admin/lost?dataSource=POLICE";
        }

        adminService.deleteLostList(nums);
        return "redirect:/admin/lost?dataSource=USER";
    }

    /**
     * 관리자 습득물 게시글 목록
     * - dataSource 값에 따라 사용자 등록 / 외부 연동 목록 조회
     * - 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/found")
    public String foundList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        int size = 10;
        int blockSize = 5;
        int totalCount;

        if ("POLICE".equals(dataSource)) {
            model.addAttribute("foundList", adminService.getPoliceFoundPage(page, size));
            totalCount = adminService.countPoliceFound();
        } else {
            model.addAttribute("foundList", adminService.getFoundPage(page, size));
            totalCount = adminService.countFound();
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startPage = ((page - 1) / blockSize) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPages);

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("boardType", "found");

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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
            return "redirect:/admin/found?dataSource=POLICE";
        }

        adminService.deleteFoundList(nums);
        return "redirect:/admin/found?dataSource=USER";
    }

    /**
     * 관리자 공지사항 목록
     * - 관리자 작성 공지사항 목록 조회
     * - 한 화면에 10개씩 페이징 처리
     */
    @GetMapping("/notice")
    public String noticeList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        int size = 10;
        int blockSize = 5;

        model.addAttribute("noticeList", adminService.getNoticePage(page, size));

        int totalCount = adminService.countNotice();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startPage = ((page - 1) / blockSize) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPages);

        model.addAttribute("boardType", "notice");
        model.addAttribute("dataSource", "ADMIN");

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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
