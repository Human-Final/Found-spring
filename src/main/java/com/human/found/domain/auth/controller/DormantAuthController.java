package com.human.found.domain.auth.controller;

import java.security.SecureRandom;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DormantAuthController {

    private final UserMapper userMapper;
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/api/send-dormant-auth-email")
    public ResponseEntity<String> sendDormantAuthEmail(
            HttpSession session) {

        String userId = (String) session.getAttribute(
                "dormantPendingUserId"
        );

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("no_request");
        }

        UserVO user = userMapper.findById(userId);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("user_not_found");
        }

        if (!"dormant".equals(user.getStatus())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("not_dormant");
        }

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        long expiresAt =
                System.currentTimeMillis() + (3 * 60 * 1000);

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject(
                    "[분실물센터] 휴면 계정 해제 인증번호입니다."
            );

            message.setText(
                    "안녕하세요. FOUND AI 기반 분실물 찾기 서비스입니다.\n\n"
                    + "휴면 계정 해제를 위한 인증번호 6자리는 ["
                    + code
                    + "] 입니다.\n"
                    + "3분 이내에 화면에 입력해 주세요."
            );

            mailSender.send(message);

        } catch (Exception e) {
            log.error(
                    "휴면계정 인증메일 발송 실패. userId={}",
                    userId,
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("mail_error");
        }

        // 이메일 발송에 성공한 뒤 인증정보 저장
        session.setAttribute("dormantAuthCode", code);
        session.setAttribute("dormantAuthUserId", user.getId());
        session.setAttribute("dormantAuthExpires", expiresAt);

        return ResponseEntity.ok("sent");
    }

    @PostMapping("/api/verify-dormant-email-code")
    public ResponseEntity<String> verifyDormantEmailCode(
            @RequestParam String code,
            HttpSession session) {

        String savedCode =
                (String) session.getAttribute("dormantAuthCode");

        String userId =
                (String) session.getAttribute("dormantAuthUserId");

        Long expiresAt =
                (Long) session.getAttribute("dormantAuthExpires");

        // 메일 발송 요청 자체가 없었던 경우
        if (savedCode == null || userId == null || expiresAt == null) {
            return ResponseEntity.ok("no_request");
        }

        // 인증번호 만료
        if (System.currentTimeMillis() > expiresAt) {
            clearDormantAuthCodeSession(session);

            // 재발송을 위해 dormantPendingUserId는 유지
            return ResponseEntity.ok("timeout");
        }

        // 인증번호 불일치
        if (!savedCode.equals(code)) {
            return ResponseEntity.ok("wrong_code");
        }

        UserVO user = userMapper.findById(userId);

        if (user == null) {
            clearAllDormantSession(session);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("user_not_found");
        }

        if (!"dormant".equals(user.getStatus())) {
            clearAllDormantSession(session);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("not_dormant");
        }

        userMapper.updateReActive(userId);

        // 인증 성공 시 휴면 인증 관련 세션 전부 제거
        clearAllDormantSession(session);

        return ResponseEntity.ok("verified");
    }


    // 인증번호와 유효시간만 제거
    //dormantPendingUserId는 재발송을 위해 유지
    private void clearDormantAuthCodeSession(HttpSession session) {
        session.removeAttribute("dormantAuthCode");
        session.removeAttribute("dormantAuthUserId");
        session.removeAttribute("dormantAuthExpires");
    }

     //휴면 인증 절차가 끝났을 때 관련 세션 전부 제거
    private void clearAllDormantSession(HttpSession session) {
        session.removeAttribute("dormantPendingUserId");
        session.removeAttribute("dormantAuthCode");
        session.removeAttribute("dormantAuthUserId");
        session.removeAttribute("dormantAuthExpires");
    }
}