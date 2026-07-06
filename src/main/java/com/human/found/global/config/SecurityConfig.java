package com.human.found.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.human.found.domain.auth.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            // 사용자 인증 정보 조회
            .userDetailsService(customUserDetailsService)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/mypage/verify-password",
                                        "/mypage/api/**",
                                        "/api/public/**",
                                        "/find-id/**"

                )
             )
             
            .authorizeHttpRequests(auth -> auth

                // 공지사항 작성/수정/삭제는 관리자 + 담당자만
                .requestMatchers(
                    "api/notices/write",
                    "api/notices/edit",
                    "api/notices/delete"
                ).hasAnyRole("MANAGER", "ADMIN")

                // 게시글 작성/수정/삭제/마이페이지는 로그인 사용자만
                .requestMatchers(
                    "/lost/*/edit",
                    "/lost/*/delete",
                    "/api/write",
                    "/found/*/edit",
                    "/found/*/delete",
                    "/api/found/delete/**",
                    "/api/lost/delete/**",
                    "/mypage/**"
                ).authenticated()

                // 채팅은 로그인 사용자만
                .requestMatchers("/chat/**").authenticated()

                // 관리자 페이지는 관리자 + 담당자만
                .requestMatchers("/admin/**").hasAnyRole("MANAGER", "ADMIN")

                // 누구나 접근 가능
                .requestMatchers(
                    "/",
                    "/login",
                    "/join",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/api/notices/list",
                    "/api/notices/detail",
                    "/lost/**",
                    "/found/**",
                    "/check-email",
                    "/api/public/send-auth-email", 
                    "/api/public/verify-email-code",
                    "/test/**",
                    "/user/welcome"
                ).permitAll()

                // 나머지는 모두 허용
                .anyRequest().permitAll()
            )

            // 로그인 설정
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("id")
                .passwordParameter("pw")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // 개발용 로그인 유지
            .rememberMe(remember -> remember
                .key("found-dev-remember-me-key")
                .tokenValiditySeconds(60 * 60)
                .rememberMeParameter("remember-me")
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}