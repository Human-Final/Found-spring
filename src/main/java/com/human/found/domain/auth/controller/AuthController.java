package com.human.found.domain.auth.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.human.found.domain.user.service.UserService;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserService userService;

    /**
     * 로그인 페이지 이동
     * URL : /login
     * Method : GET
     */
    @GetMapping("/login")
    public String loginPage(
        @RequestParam(value = "error", required = false) String error, 
        @RequestParam(name = "withdraw", required = false) String withdraw,
        Model model) {
            if(withdraw != null) {
                model.addAttribute("message", "회원탈퇴가 완료되었습니다.");
            }
            if(error != null) {
                model.addAttribute("loginError", true);
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
        model.addAttribute("userId", user.getId());

        // 회원가입 처리
        userService.join(user);

        // 회원가입 완료 후 로그인 페이지 이동
        return "user/welcome";
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

    // 이메일 중복확인
    @ResponseBody
    @GetMapping("/check-email")
    public String checkEmail(@RequestParam("email") String email) {

        boolean duplicated = userService.isDuplicatedEmail(email);

        if (duplicated) {
            return "duplicated";
        }

        return "available";
    }
    
    // 회원가입 할 때 인증메일 발송하기
    @ResponseBody
    @PostMapping("/api/public/send-auth-email")
    public String sendJoinAuthEmail(@RequestParam("email") String email, HttpSession session) {
        return userService.sendJoinEmail(email, session);
    }

    // 회원가입 하는 유저의 이메일 인증코드 전송을 처리
    @ResponseBody
    @PostMapping("/api/public/verify-email-code") //
    public String verifyJoinEmailCode(@RequestParam("code") String inputCode, @RequestParam("email") String email, HttpSession session) {
        return userService.verifyJoinCode(inputCode, email, session);
    }

    @GetMapping("/find-id")
    public String findIdPage() {
        return "user/findid";
    }

    @ResponseBody
    @PostMapping("/find-id")
    public String findIdData(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String email = params.get("email");

        String foundId = userService.findUserId(name, email);

        if(foundId==null){
            return null;
        }
        return foundId;
    }

    @GetMapping("/find-pw")
    public String findPwPage() {
        return "user/findpw";
    }

    @ResponseBody
    @PostMapping("/find-pw")
    public String sendPwAuthEmail(@RequestParam("userId") String userId, 
                                  @RequestParam("name") String name, 
                                  @RequestParam("email") String email, HttpSession session) {
        
        // 1. MariaDB 조회로 3개 정보 일치 회원 확인
        boolean isUserExist = userService.isUserExist(userId,name,email);
        if (!isUserExist) {
            return "no_user"; 
        }

        // 2. 일치하면 유저 ID 세션 저장 후 이메일 발송 로직 실행
        session.setAttribute("pwResetUserId", userId);
        return userService.sendPwEmail(email, session);
    }

    // 비밀번호 찾기 인증번호 검증 완료 처리
    @ResponseBody
    @PostMapping("/find-pw/verify-code")
    public String verifyPwEmailCode(@RequestParam("code") String inputCode, 
                                    @RequestParam("email") String email, 
                                    HttpSession session) {
        String result = userService.verifyPwCode(inputCode, email, session);
        return result; 
    }

    // 비밀번호 변경한 것 DB로 인코딩하여 보내기
    @ResponseBody
    @PostMapping("/find-pw/reset") // 💡 자바스크립트 fetch 주소인 '/find-pw/reset'과 완벽 일치
    public String resetPassword(@RequestParam("password") String newPassword, HttpSession session) {
        String userId = (String) session.getAttribute("pwResetUserId");

        userService.updateUserPassword(userId, newPassword);
        return "SUCCESS";
    }

}
