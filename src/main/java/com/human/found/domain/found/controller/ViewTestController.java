package com.human.found.domain.found.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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


}
