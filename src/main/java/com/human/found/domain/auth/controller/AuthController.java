package com.human.found.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String loginPage( 
        @RequestParam(required = false) String withdraw,
        Model model) {
            
            if(withdraw != null) {
                model.addAttribute("message", "회원탈퇴가 완료되었습니다.");
            }

        return "user/login";
        }

    /**
     * 회원가입 페이지 이동
     * URL : /join
     * Method : GET
     */
    @GetMapping("/join")
    public String joinPage() {
        return "user/join";
    }

    /**
     * 회원가입 처리
     * URL : /join
     * Method : POST
     */
    @PostMapping("/join")
    public String join(UserVO user, Model model) {

        // 회원가입 입력값 검증
        // - 아이디 중복 여부
        // - 이메일 중복 여부
        // - 비밀번호 확인 일치 여부
        // - 비밀번호 정책 검사
        String error = userService.validateJoin(user);

        // 검증 실패 시 회원가입 페이지로 이동
        // 에러 메시지를 화면에 출력
        if (error != null) {
            model.addAttribute("error", error);
            return "user/join";
        }

        // 회원가입 처리
        userService.join(user);

        // 회원가입 완료 후 로그인 페이지 이동
        return "redirect:/login";
    }

    /**
     * 아이디 중복검사
     * URL : /check-id
     * Method : GET
     */
    @ResponseBody
    @GetMapping("/check-id")
    public String checkId(@RequestParam("id") String id) {

        boolean duplicated = userService.isDuplicatedId(id);

        if (duplicated) {
            return "duplicated";
        }

        return "available";
    }
}
