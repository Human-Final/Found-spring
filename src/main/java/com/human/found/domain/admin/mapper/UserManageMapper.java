package com.human.found.domain.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.user.vo.UserVO;

@Mapper
public interface UserManageMapper {
    
    // 탈퇴한 회원까지 조회해서 아이디 받아오기(where deleted = 0 없는 쿼리문)
    UserVO findByIdIncludeDeleted(@Param("id") String id);

    // 회원 전체 조회
    List<UserVO> totalUserList();
    
    // 회원 정보 변경
    int updateUserById(
            @Param("id") String userId,
            @Param("name") String name,
            @Param("email") String email,
            @Param("tel") String tel,
            @Param("status") String status,
            @Param("isDeleted") int isDeleted,
            @Param("role") String role,
            @Param("canUpdateRole") boolean canUpdateRole
    );
    
    // 회원 조회(검색/필터)
    List<UserVO> searchUsers(UserSearchConditionDTO conditionDTO);

    // 조회된 회원 수 카운트
    int countUsers(UserSearchConditionDTO conditionDTO);

    // 회원 엑셀 다운로드용(페이징 없음)
    List<UserVO> userInfoDownload(UserSearchConditionDTO conditionDTO);

    // 관리자 페이지에서 회원 추가
    int insertUserByAdmin(UserVO userVO);

    int countAdmin();
}
