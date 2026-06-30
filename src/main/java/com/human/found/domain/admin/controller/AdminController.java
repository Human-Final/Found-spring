package com.human.found.domain.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 관리자 페이지 분실물 게시글 목록
    @GetMapping("/lost")
    public String lostList(Model model) {
        model.addAttribute("lostList", adminService.getLostList());

        return "admin/lost";
    }

    // 관리자 분실물 게시글 삭제
    @PostMapping("/lost/delete")
    public String deleteLost(@PathVariable Long num) {

        adminService.deleteLost(num);

        return "redirect:/admin/lost";
    }

    // 관리자 분실물 선택 삭제
    @PostMapping("/lost/delete2")
    public String deleteLostList(@RequestParam("nums") List<Long> nums) {

        adminService.deleteLostList(nums);

        return "redirect:/admin/lost";
    }    
    
    
}
