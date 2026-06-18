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
    
    // 회원정보 수정할 때 사용하는 쿼리매핑
    void updateUser(UserVO user);

    // 회원탈퇴 처리 (실제 삭제는 아니고 is_deleted, deleted_at변경)
    int withdrawUser(String id);
}
