package com.human.found.domain.home.Controller;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class HomeController {
    
    @GetMapping("/")
    public String getMethodName() {
        return "home/home";
    }
    

}
