package com.human.found.domain.user.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.FoundCommentService;
import com.human.found.domain.comment.service.LostCommentService;
import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.user.service.UserServiceImpl;
import com.human.found.domain.user.vo.MyPagePostVO;
import com.human.found.domain.user.vo.UserVO;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/mypage")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private FoundCommentService foundCommentService;

    @Autowired
    private LostCommentService lostCommentService;

    @Autowired 
    private JavaMailSender mailSender; 

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

        // 3. 로그인하는 유저가 작성한 글 통계 데이터 갖고오기
        java.util.Map<String, Object> counts = userService.getMyPageCounts(loginUserId);

        // 4. 유저가 작성한 최신 글들의 정보 2개를 리스트화하여 DB에서 조회함
        List<MyPagePostVO> recentPosts = userService.getMyRecentPostList(loginUserId);

        // 5. 유저가 작성한 모든 글들의 정보를 리스트화해서 DB에서 조회하기
        List<MyPagePostVO> allPosts = userService.getMyAllPostList(loginUserId);

        // 6. 유저가 작성한 모든 글들의 정보를 리스트화해서 DB에서 조회하기

        List<CommentVO> allComments = userService.findAllCommentsByUserId(loginUserId);
        
        // 7. 중요: 화면(Thymeleaf)으로 데이터를 던져줍니다.
        model.addAttribute("user", user);
        model.addAttribute("counts", counts);
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("allPosts", allPosts);
        model.addAttribute("allComments", allComments);
        // 8. 마이페이지 파일 경로를 리턴합니다.
        return "user/mypage";
    }

    /**
     * 현재 비밀번호 Ajax 본인 인증 API (데이터 유실 방지 가드 적용)
     */
    @PostMapping("/verify-password")
    @ResponseBody 
    public boolean verifyPassword(HttpServletRequest request, Principal principal) {
        if (principal == null) return false;

        // HttpServletRequest를 통해 자바스크립트가 보낸 값을 직접 안전하게 추출
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
    
     /**
     * - 로그인한 사용자 ID 확인
     * - 입력한 비밀번호 검증
     * - 회원정보 소프트 삭제
     * - 세션 무효화 후 로그인 페이지 이동
     */

    @ResponseBody
    @GetMapping("/api/check-email")
    public String checkEmail(@RequestParam("email") String email) {

        // true면 중복, false면 사용 가능
        boolean isDuplicated = userService.isDuplicatedEmail(email); 
        
        if (isDuplicated) {
            return "duplicated"; // 중복됨
        }

        return "available"; // 사용 가능
    }

    /**
     * ✉️ [신규 개설] 중복 확인 통과 후, 파란 버튼을 눌렀을 때만 진짜 메일을 발송하는 API
     */
    @ResponseBody
    @PostMapping("/api/send-auth-email") // 👈 진짜 메일을 쏘는 전용 무전기 주소를 새로 팝니다.
    public String sendAuthEmail(@RequestParam("email") String email, HttpSession session) {
        
        // 🎲 암호학적으로 안전한 6자리 난수 생성
        java.util.Random random = new java.util.Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));
        
        // 🕒 3분 만료 타임스탬프 설정 (현재 시간 + 180,000ms)
        long expiresAt = System.currentTimeMillis() + (3 * 60 * 1000);

        // 💾 서버 세션(HttpSession)에 인증 코드와 만료 시각 임시 보관
        session.setAttribute("emailAuthCode", verificationCode);
        session.setAttribute("emailAuthTarget", email);
        session.setAttribute("emailAuthExpires", expiresAt);

        // 📝 진짜 이메일 제목과 본문 내용 조립하여 발송
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            
            message.setTo(email); // 수신자 주소 세팅
            message.setSubject("[유실물센터] 이메일 인증번호입니다."); 
            message.setText("안녕하세요. FOUND AI 기반 분실물 찾기 서비스입니다.\n"
                    + "요청하신 이메일 인증번호 6자리는 [" + verificationCode + "] 입니다.\n"
                    + "3분 이내에 화면에 입력해 주세요.");
            
            mailSender.send(message); // 🚀 실제로 구글 SMTP 서버를 거쳐 메일 발송!
            
            System.out.println("=========================================");
            System.out.println("✉️ [메일 엔진] 파란 버튼 신호 수신! 실제 발송 완수!");
            System.out.println("🔑 생성된 6자리 번호: " + verificationCode);
            System.out.println("=========================================");
            
        } catch (Exception e) {
            System.out.println("❌ [메일 엔진] 구글 SMTP 서버 발송 실패. 오류 내용:");
            e.printStackTrace(); 
            return "mail_error";
        }

        return "send_success"; // 발송 완료 신호 반환
    }

    /**
     * 🔐 주신 자바스크립트의 application/x-www-form-urlencoded (POST) 데이터 포맷을 
     * 그대로 수신하여 검증하는 정석 컨트롤러 메서드입니다.
     */
    @ResponseBody
    @PostMapping("/api/verify-email-code") // 👈 자바스크립트의 fetch 경로와 일치
    public String verifyEmailCode(
            @RequestParam("code") String inputCode, // 📌 body의 code 값을 매핑
            @RequestParam("email") String email,     // 📌 body의 email 값을 매핑
            HttpSession session) {
        
        // 1. 세션에 발송할 때 저장해둔 데이터들 드로우
        String sessionCode = (String) session.getAttribute("emailAuthCode");
        String sessionEmail = (String) session.getAttribute("emailAuthTarget");
        Long expiresAt = (Long) session.getAttribute("emailAuthExpires");

        // [가드 1] 세션 내역 부재 시 불법 접근 차단
        if (expiresAt == null || sessionCode == null || !email.equals(sessionEmail)) {
            return "no_request";
        }

        // [가드 2] 3분 제한 시간 타임아웃 검증 (현재 밀리초와 비교)
        if (System.currentTimeMillis() > expiresAt) {
            session.invalidate(); 
            return "timeout";
        }

        // [가드 3] 입력한 6자리 난수 대조
        if (!sessionCode.equals(inputCode)) {
            return "wrong_code";
        }

        // 🟢 모든 조건이 완벽히 맞으면 "verified" 글자를 리턴하여 
        // 자바스크립트의 if (data === "verified") 문을 활성화시킵니다.
        return "verified";
    }
    
    @PostMapping("/api/found/delete/{atcId}")
    public String deleteFound(@PathVariable("atcId") String atcId, Principal principal) {
        if (principal != null) {
            // 💡 각각 분리해서 만든 습득물 전용 삭제 서비스 호출
            userService.removeFoundPost(atcId); 
        }
        return "redirect:/mypage"; 
    }

    @PostMapping("/api/lost/delete/{atcId}")
    public String deleteLost(@PathVariable("atcId") String atcId, Principal principal) {
        if (principal != null) {
            // 💡 각각 분리해서 만든 분실물 전용 삭제 서비스 호출
            userService.removeLostPost(atcId);
        }
        return "redirect:/mypage"; 
    }

    // 1. 습득물 댓글 비동기 삭제 API -> 동기 리다이렉트 구조로 최종 변경
    @PostMapping("/api/found/comment/delete/{commentNum}")
    public String deleteMyPageFoundComment(@PathVariable("commentNum") Long commentNum, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        try {
            CommentVO savedComment = foundCommentService.getCommentByCommentNum(commentNum);
            if (savedComment != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isOwner = savedComment.getId().equals(principal.getName());
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_MANAGER"));

                if (isOwner || isAdmin) {
                    foundCommentService.deleteComment(commentNum);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🌟 [핵심 정답]: 하얀 화면에 Success를 띄우지 않고, 내가 보던 마이페이지 댓글 탭으로 브라우저를 튕겨 보냅니다!
        return "redirect:/mypage#myComments"; 
    }

    // 2. 분실물 댓글 비동기 삭제 API -> 동기 리다이렉트 구조로 최종 변경
    @PostMapping("/api/lost/comment/delete/{commentNum}")
    public String deleteMyPageLostComment(@PathVariable("commentNum") Long commentNum, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            CommentVO savedComment = lostCommentService.getCommentByCommentNum(commentNum);
            if (savedComment != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isOwner = savedComment.getId().equals(principal.getName());
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_MANAGER"));

                if (isOwner || isAdmin) {
                    lostCommentService.deleteComment(commentNum);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🌟 [핵심 정답]: 하얀 화면에 Success를 띄우지 않고, 내가 보던 마이페이지 댓글 탭으로 브라우저를 튕겨 보냅니다!
        return "redirect:/mypage#myComments"; 
    }

    // 본인 댓글 전체 삭제 컨트롤러
    @PostMapping("/api/comments/delete-all/{userId}")
    @ResponseBody 
    public ResponseEntity<String> deleteAllMyComments(@PathVariable("userId") String userId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            // 안전하게 가공된 실제 유저 고유 ID 계정값(예: "11")으로 일괄 삭제 서비스 쿼리를 집도합니다.
            userService.deleteAllCommentsByUserId(userId);
            
            return ResponseEntity.ok("Success"); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fail: " + e.getMessage());
        }
    }



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
                return "redirect:/mypage#memberWithdraw";
            }

        }

}
