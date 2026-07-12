package com.human.found.domain.auth.handler;

import java.io.IOException;

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

            // 로그인 성공으로 만들어진 인증 상태 제거
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            // remember-me 쿠키 제거
            deleteCookie(response, "remember-me");

            // 휴면 해제 절차에 사용할 새 세션 생성
            HttpSession newSession = request.getSession(true);
            
            // 아직 인증번호는 만들지 않고 대상 사용자만 저장
            newSession.setAttribute(
                    "dormantPendingUserId",
                    user.getId()
            );


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
