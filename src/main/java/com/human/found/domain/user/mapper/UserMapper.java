package com.human.found.domain.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.user.vo.UserVO;

@Mapper
public interface UserMapper {
    // 아이디로 회원 조회
    UserVO findById(@Param("id")String id);

    // 회원가입
    void insertUser(UserVO user);

    // 아이디 중복검사
    int countById(@Param("id") String id);
    
    // 이메일 중복 확인
    int countByEmail(@Param("email") String email);
}
