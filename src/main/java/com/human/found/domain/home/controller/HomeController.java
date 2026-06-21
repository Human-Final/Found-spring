package com.human.found.domain.home.controller;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import com.human.found.domain.home.service.HomeService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    
    @GetMapping("/")
    public String homeView(Model model) {

        model.addAttribute(
            "recentFoundList", homeService.recentFoundList());
        model.addAttribute(
            "recentLostList", homeService.recentLostList());
        model.addAttribute(
            "countWeeklyFound", homeService.countWeeklyFound());
        model.addAttribute(
            "countWeeklyLost", homeService.countWeeklyLost());
        model.addAttribute(
            "countWeeklyDone", homeService.countWeeklyDone());

        return "home/home";
    }

    @GetMapping("/logout")
    public String logoutsuccess() {
        return "redirect:/";
    }   

}