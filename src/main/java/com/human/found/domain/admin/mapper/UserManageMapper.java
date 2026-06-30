package com.human.found.domain.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.user.vo.UserVO;

@Mapper
public interface UserManageMapper {
    
    // 유저 권한 변경
    int updateUserStatusByIds(@Param("userIds") List<String> userIds, 
                             @Param("status") String status,
                             @Param("isDeleted") int isDeleted);

    // 유저 전제 조회
    List<UserVO> totalUserList();
}
