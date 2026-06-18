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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ResponseBody;


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

        // 3. 🌟 중요: 화면(Thymeleaf)으로 데이터를 던져줍니다.
        model.addAttribute("user", user);

        // 4. 마이페이지 파일 경로를 리턴합니다.
        return "user/mypage";
    }

    /**
     * 🔒 현재 비밀번호 Ajax 본인 인증 API (데이터 유실 방지 가드 적용)
     */
    @PostMapping("/verify-password")
    @ResponseBody 
    public boolean verifyPassword(HttpServletRequest request, Principal principal) {
        if (principal == null) return false;

        // 📌 HttpServletRequest를 통해 자바스크립트가 보낸 값을 직접 안전하게 추출
        String pwCheck = request.getParameter("pwCheck");
        
        // 디버깅 출력을 심어 실제로 브라우저가 보낸 글자가 서버에 도착했는지 확인합니다.
        System.out.println("=========================================");
        System.out.println("⌨️ [컨트롤러 수신 확인] pwCheck 원본 값: [" + pwCheck + "]");
        if(pwCheck != null) {
            System.out.println("⌨️ [컨트롤러 수신 확인] 글자 수: " + pwCheck.length());
        }
        System.out.println("=========================================");

        if (pwCheck == null || pwCheck.trim().isEmpty()) {
            System.out.println("❌ [인증 실패] 브라우저에서 보낸 비밀번호 데이터가 유실되었습니다.");
            return false;
        }

        String loginUserId = principal.getName(); // 로그인한 아이디 ("11")
        
        // 서비스단 호출 (양쪽 공백을 잘라내서 안전하게 전달)
        return userService.checkPassword(loginUserId, pwCheck.trim()); 
    }

    /**
     * 모달창에서 전송한 새 비밀번호 변경 처리 (규칙 미달 시 예외 캐치)
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("pw") String newPw, Principal principal, RedirectAttributes redirectAttributes) {
        // 📌 [디버깅 추가] 컨트롤러 진입 확인용
        System.out.println("=========================================");
        System.out.println("🛸 [컨트롤러] /mypage/change-password 매핑 주소 호출 성공!");
        System.out.println("👤 로그인된 아이디(Principal): " + (principal != null ? principal.getName() : "null"));
        System.out.println("⌨️ 컨트롤러가 가로챈 새 패스워드: [" + newPw + "]");
        System.out.println("=========================================");

        if (principal == null) return "redirect:/login";

        String loginUserId = principal.getName();
        
        try {
            // 📌 서비스단을 호출하여 유효성 검사 및 패스워드 암호화 변경 동시 수행
            userService.updateUserPassword(loginUserId, newPw);
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            // 비밀번호 조합(8자 미만, 3가지 조합 미달 등) 예외 발생 시 안내 메시지 송출
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 변경 중 알 수 없는 시스템 오류가 발생했습니다.");
        }

        return "redirect:/mypage#memberEdit"; 
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


}
