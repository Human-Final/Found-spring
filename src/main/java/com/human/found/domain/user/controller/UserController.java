package com.human.found.domain.user.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.user.service.UserService;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class UserController {
    
    @Autowired
    private UserService userService;

    // /mypage 또는 /mypage/ 로 요청이 들어왔을 때 마이페이지 화면을 보여줍니다.
    @GetMapping({"", "/"})
    public String mypageView(Principal principal, Model model) {
        
        // 1. 스프링 시큐리티에서 현재 로그인한 유저의 아이디("11")를 꺼냅니다.
        if (principal == null) {
            return "redirect:/login"; // 혹시나 로그인이 안 되어 있다면 로그인창으로 복귀
        }
        String loginUserId = principal.getName(); 

        // 2. DB에서 유저 정보(UserVO)를 가져옵니다.
        UserVO user = userService.getUserInfo(loginUserId);

        // 3. 중요: 화면(Thymeleaf)으로 데이터를 던져줍니다.
        model.addAttribute("user", user);

        // 4. 마이페이지 파일 경로를 리턴합니다.
        return "user/mypage";
    }


    // 회원정보 수정 POST 매핑
    @PostMapping("/profile")
    public String updateProfile(
            UserVO userVO, // HTML의 name 값들이 UserVO의 필드(email, tel, pw, pwCheck)에 자동 매핑됩니다.
            Principal principal, // [수정] 세션 대신 시큐리티 Principal을 사용하여 인증 유저 ID를 정확히 가져옵니다.
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }
        
        String loginUserId = principal.getName(); // 로그인한 아이디 ("11")
        userVO.setId(loginUserId); // 변경 요청 객체에 아이디 강제 주입

        try {
            // 3단계 서비스 호출: 데이터 전송 방식을 VO 통째로 넘기도록 변경합니다.
            userService.updateUserInfo(userVO);
            
            // 수정 성공 메시지
            redirectAttributes.addFlashAttribute("successMessage", "회원정보가 성공적으로 변경되었습니다.");

        } catch (IllegalArgumentException e) {
            // 현재 비밀번호 불일치 등의 예외 발생 시 오류 메시지를 들고 복귀
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage";
        }

        return "redirect:/mypage";
    }

    /**
     * - 로그인한 사용자 ID 확인
     * - 입력한 비밀번호 검증
     * - 회원정보 소프트 삭제
     * - 세션 무효화 후 로그인 페이지 이동
     */

    @PostMapping("/withdraw")
    public String withdrawUser(
        @RequestParam("password") String password,
        Principal principal,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes) {

            // 로그인 여부 확인
            if(principal == null) {
                return "redirect:/login";
            }

            // 로그인한 사용자 ID 가져오기
            String loginUserId = principal.getName();

            try {
                // 비밀번호 확인 후 회원탈퇴 처리
                userService.withdrawUser(loginUserId, password);

                // 현재 세션 가져오기
                HttpSession session = request.getSession(false);

                // 세션 있을 시 무효화
                if(session != null) {
                    session.invalidate();
                }

                // 로그인 페이지로 이동
                return "redirect:/login?withdraw";
            
            } catch (IllegalArgumentException e) {

                // 비밀번호 불일치 시 등 오류 발생 시 마이페이지로 복귀
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/login?withdraw";
            }

        }

}
