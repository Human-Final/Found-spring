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
}
