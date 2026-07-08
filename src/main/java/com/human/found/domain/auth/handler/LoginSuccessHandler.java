package com.human.found.domain.auth.handler;

import java.io.IOException;
import java.security.SecureRandom;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
        
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    
    @Override
    public void onAuthenticationSuccess(
                HttpServletRequest request, 
                HttpServletResponse response,
            Authentication authentication) throws IOException {

        String loginId = authentication.getName();
        UserVO user = userMapper.findById(loginId);

        if (user == null || user.getIsDeleted() == 1) {
            SecurityContextHolder.clearContext();

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect("/login?error");
            return;
        }

        if("dormant".equals(user.getStatus())){
            String code = String.format("%06d", new SecureRandom().nextInt(1000000));
            long expiresAt = System.currentTimeMillis() + (3 * 60 * 1000);

            try {
                SimpleMailMessage message = new SimpleMailMessage();

                message.setTo(user.getEmail());
                message.setSubject("[분실물센터] 휴면 계정 해제 인증번호입니다.");
                message.setText("안녕하세요. FOUND AI 기반 분실물 찾기 서비스입니다.\n\n"
                        + "휴면 계정 해제를 위한 인증번호 6자리는 ["
                        + code
                        + "] 입니다.\n"
                        + "3분 이내에 화면에 입력해 주세요.");

                mailSender.send(message);

            } catch (Exception e) {
                e.printStackTrace();

                new SecurityContextLogoutHandler().logout(request, response, authentication);
                deleteCookie(response, "remember-me");
                deleteCookie(response, "JSESSIONID");

                response.sendRedirect("/login?mailError=true");
                return;
            }

            // 로그인 성공으로 만들어진 인증 상태 제거
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            // remember-me 쿠키 제거
            deleteCookie(response, "remember-me");


            // logout으로 세션이 날아간 뒤 새 세션 생성
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("dormantAuthCode", code);
            newSession.setAttribute("dormantAuthUserId", user.getId());
            newSession.setAttribute("dormantAuthExpires", expiresAt);

            response.sendRedirect("/login?dormant=true");
            return;
        }


        if (!"active".equals(user.getStatus())) {
            SecurityContextHolder.clearContext();

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect("/login?error");
            return;
        }

        userMapper.updateLastLoginAt(loginId);
        response.sendRedirect("/");
    }   

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
 
}
