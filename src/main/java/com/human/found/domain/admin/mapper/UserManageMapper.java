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

    // 유저 전체 조회
    List<UserVO> totalUserList();
    
    // 유저 상태 변경
    int updateUserStatusById(@Param("id") String id, 
                              @Param("status") String status,
                              @Param("isDeleted") int isDeleted);

    // 유저 권한 변경
    int updateUserRoleById(@Param("id") String id, 
                           @Param("role") String role);

    // 유저 정보 변경
    int updateUserProfileById(@Param("id") String id,
                             @Param("name") String name,
                             @Param("email") String email,
                             @Param("tel") String tel);

                            
    List<UserVO> searchUsers(UserSearchConditionDTO conditionDTO);

    int countUsers(UserSearchConditionDTO conditionDTO);

    List<UserVO> userInfoDownload(UserSearchConditionDTO conditionDTO);
}
