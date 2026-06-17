package com.human.found.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class UserController {
    
    // /mypage 또는 /mypage/ 로 요청이 들어왔을 때 마이페이지 화면을 보여줍니다.
    @GetMapping({"", "/"})
    public String mypageView() {
        return "user/mypage";
    }
}
