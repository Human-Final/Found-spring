package com.human.found.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 
     * - 비밀번호 암호화
     * - 회원 정보 저장
     */
    public void join(UserVO user) {
        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(user.getPw());
        user.setPw(encodedPw);

        // 회원 저장
        userMapper.insertUser(user);
    }

    /**
     * 아이디로 회원 조회
     * 로그인 시 Security에서 사용
     */
    public UserVO findById(String id) {
        return userMapper.findById(id);
    }
}
