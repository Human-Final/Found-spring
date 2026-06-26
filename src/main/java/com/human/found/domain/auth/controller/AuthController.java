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

import com.human.found.domain.user.service.UserServiceImpl;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private JavaMailSender mailSender;
    private final UserServiceImpl userService;

    /**
     * 로그인 페이지 이동
     * URL : /login
     * Method : GET
     */
    @GetMapping("/login")
    public String loginPage( 
        @RequestParam(name = "withdraw", required = false) String withdraw,
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
    
    // =================================================================
    // 👤 [회원가입 전용 - 추가] 비로그인 전용 실시간 메일 인증 엔진
    // =================================================================
    
    @ResponseBody
    @PostMapping("/api/public/send-auth-email") // 👈 가입창 전용 완전 공개 주소 개설!
    public String sendJoinAuthEmail(@RequestParam("email") String email, HttpSession session) {
        java.util.Random random = new java.util.Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));
        long expiresAt = System.currentTimeMillis() + (3 * 60 * 1000);

        // 마이페이지 세션과 충돌하지 않도록 가입 전용 키값('join')을 붙여 봉인합니다.
        session.setAttribute("joinEmailAuthCode", verificationCode);
        session.setAttribute("joinEmailAuthTarget", email);
        session.setAttribute("joinEmailAuthExpires", expiresAt);

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[FOUND-AI기반 내 물건 찾기 서비스] 회원가입 이메일 인증번호입니다."); 
            message.setText("안녕하세요. 회원가입을 환영합니다.\n"
                    + "요청하신 가입 인증번호 6자리는 [" + verificationCode + "] 입니다.\n"
                    + "3분 이내에 화면에 입력해 주세요.");
            mailSender.send(message);
            
            System.out.println("👤 [회원가입 메일엔진] 생성된 6자리 번호: " + verificationCode);
        } catch (Exception e) {
            e.printStackTrace(); 
            return "mail_error";
        }
        return "send_success";
    }

    @ResponseBody
    @PostMapping("/api/public/verify-email-code") // 👈 가입창 전용 검증 주소 개설!
    public String verifyJoinEmailCode(@RequestParam("code") String inputCode, @RequestParam("email") String email, HttpSession session) {
        String sessionCode = (String) session.getAttribute("joinEmailAuthCode");
        String sessionEmail = (String) session.getAttribute("joinEmailAuthTarget");
        Long expiresAt = (Long) session.getAttribute("joinEmailAuthExpires");

        if (expiresAt == null || sessionCode == null || !email.equals(sessionEmail)) return "no_request";
        if (System.currentTimeMillis() > expiresAt) { session.invalidate(); return "timeout"; }
        if (!sessionCode.equals(inputCode)) return "wrong_code";

        return "verified";
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
            return "찾으시는 아이디가 없습니다";
        }
        return foundId;
    }
    
    

    
}
