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
     */
    @GetMapping("/lost")
    public String lostList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            Model model) {

        if ("POLICE".equals(dataSource)) {
            model.addAttribute("lostList", adminService.getPoliceLostList());
        } else {
            model.addAttribute("lostList", adminService.getLostList());
        }

        model.addAttribute("dataSource", dataSource);
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
            return "redirect:/admin/lost?dataSource=POLICE";
        }

        adminService.deleteLostList(nums);
        return "redirect:/admin/lost?dataSource=USER";
    }

    /**
     * 관리자 습득물 게시글 목록
     * - dataSource 값에 따라 사용자 등록 / 외부 연동 목록 조회
     */
    @GetMapping("/found")
    public String foundList(
            @RequestParam(value = "dataSource", required = false, defaultValue = "USER") String dataSource,
            Model model) {

        if ("POLICE".equals(dataSource)) {
            model.addAttribute("foundList", adminService.getPoliceFoundList());
        } else {
            model.addAttribute("foundList", adminService.getFoundList());
        }

        model.addAttribute("dataSource", dataSource);
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
            return "redirect:/admin/found?dataSource=POLICE";
        }

        adminService.deleteFoundList(nums);
        return "redirect:/admin/found?dataSource=USER";
    }

    // 관리자 공지사항 목록
    @GetMapping("/notice")
    public String noticeList(Model model) {

        model.addAttribute("noticeList", adminService.getNoticeList());
        model.addAttribute("boardType", "notice");
        model.addAttribute("dataSource", "ADMIN");

        return "admin/adminPage";
    }

    // 관리자 공지사항 선택 삭제
    @PostMapping("/notice/delete")
    public String deleteNoticeList(
            @RequestParam(value = "nums", required = false) List<Long> nums) {

        adminService.deleteNoticeList(nums);

        return "redirect:/admin/notice";
    }
}