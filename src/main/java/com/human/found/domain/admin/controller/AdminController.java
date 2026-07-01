package com.human.found.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AdminController {

    @GetMapping("/api/admin/board")
    public String board(Model model) {
         model.addAttribute("startPage", 1);
        model.addAttribute("endPage", 5);
        model.addAttribute("page", 1);
        model.addAttribute("totalPages", 5);


        return "admin/board";
    }

    @GetMapping("/test")
    public String board2() {
        return "admin/test";
    }
    
    

}
