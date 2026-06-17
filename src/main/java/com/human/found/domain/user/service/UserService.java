package com.human.found.domain.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 아이디 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    public boolean isDuplicatedId(String id) {
        return userMapper.countById(id) > 0;
    }

    /**
     * 이메일 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    public boolean isDuplicatedEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }


    // 회원가입 입력값 검증
    public String validateJoin(UserVO user) {

        // 아이디 중복
        if (isDuplicatedId(user.getId())) {
            return "이미 사용중인 아이디입니다.";
        }

        // 이메일 중복
        if (isDuplicatedEmail(user.getEmail())) {
            return "이미 사용중인 이메일입니다.";
        }

        // 비밀번호 확인
        if (!user.getPw().equals(user.getPwCheck())) {
            return "비밀번호가 일치하지 않습니다.";
        }

        // 비밀번호 길이
        if (user.getPw().length() < 8) {
            return "비밀번호는 8자 이상이어야 합니다.";
        }

        // 비밀번호 조합 검사
        // 대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함
        int count = 0;

        if (user.getPw().matches(".*[A-Z].*")) count++;
        if (user.getPw().matches(".*[a-z].*")) count++;
        if (user.getPw().matches(".*[0-9].*")) count++;
        if (user.getPw().matches(".*[^a-zA-Z0-9].*")) count++;

        if (count < 3) {
            return "대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함해야 합니다.";
        }

        // 검증 통과
        return null;
    }

    @Transactional
    public void updateUserInfo(UserVO userVO) {
        // 1. DB에서 현재 로그인한 유저의 원본 암호 정보 가져오기
        UserVO dbUser = userMapper.findById(userVO.getId());
        
        // 2. 입력한 현재 비밀번호(pwCheck)와 DB에 암호화되어 저장된 비밀번호(pw)가 일치하는지 검증
        // passwordEncoder.matches(평문비밀번호, 암호화된비밀번호)
        if (!passwordEncoder.matches(userVO.getPwCheck(), dbUser.getPw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호를 입력한 경우에만 암호화해서 세팅, 입력 안 했으면 기존 암호 유지
        if (userVO.getPw() != null && !userVO.getPw().trim().isEmpty()) {
            String encodedNewPw = passwordEncoder.encode(userVO.getPw());
            userVO.setPw(encodedNewPw);
        } else {
            userVO.setPw(dbUser.getPw()); // 새 비밀번호 입력 안 했으면 기존 암호 그대로 바구니에 담기
        }

        // 4. DB 업데이트 실행
        userMapper.updateUser(userVO);
    }

    // 마이페이지 뷰를 띄울 때 회원 정보를 안전하게 꺼내오기 위한 메서드
    public UserVO getUserInfo(String id) {
        return userMapper.findById(id);
    }

}
