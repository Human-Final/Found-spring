package com.human.found.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.human.found.domain.user.service.UserService;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    /**
     * 로그인 페이지 이동
     * URL : /login
     * Method : GET
     */
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    /**
     * 회원가입 페이지 이동
     * URL : /join
     * Method : GET
     */
    @GetMapping("/join")
    public String joinPage(){
        return "user/join";
    }

    /**
     * 회원가입 처리
     * URL : /join
     * Method : POST
     */
    @PostMapping("/join")
    public String join(UserVO user) {
        
        // 회원가입 처리
        userService.join(user);

        // 회원가입 완료 후 로그인 페이지 이동
        return "redirect:/login";
    }
    
}
