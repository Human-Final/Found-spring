package com.human.found.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.human.found.domain.home.service.HomeService;
import com.human.found.domain.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final NoticeService noticeService;
    
    @GetMapping("/")
    public String homeView(Model model) {

        model.addAttribute(
            "recentFoundList", homeService.recentFoundList());
        model.addAttribute(
            "recentLostList", homeService.recentLostList());
        model.addAttribute(
            "recentNotices", homeService.recentNotices());
        model.addAttribute(
            "countWeeklyFound", homeService.countWeeklyFound());
        model.addAttribute(
            "countWeeklyLost", homeService.countWeeklyLost());
        model.addAttribute(
            "countWeeklyDone", homeService.countWeeklyDone());
        model.addAttribute("popupNoticeList", noticeService.getActivePopups());
        
        return "home/home";
    }

    @GetMapping("/logout")
    public String logoutsuccess() {
        return "redirect:/";
    }   

}