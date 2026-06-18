package com.human.found.domain.found.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class ViewTestController {
    
    @GetMapping("test/api/lost")
    public String lostBoard() {
        return "lost/list";
    }

    @GetMapping("test/api/found")
    public String foundBoard() {
        return "found/list";
    }

    // @GetMapping("/mypage")
    // public String mypage() {
    //     return "user/mypage";
    // }

    // @GetMapping("/api/write")
    // public String writeForm() {
    //     // model.addAttribute("postForm", new PostForm());
    //     // model.addAttribute("categories", categories());
    //     return "found/write";
    // }


}
