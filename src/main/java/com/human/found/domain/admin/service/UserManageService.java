package com.human.found.domain.admin.service;

import java.io.IOException;
import java.util.List;

import com.human.found.domain.admin.dto.UserBulkInfoDTO;
import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpServletResponse;

public interface UserManageService {
    
    List<UserVO> totalUserList();

    int updateUserBulk(
            UserBulkInfoDTO userInfo, 
            boolean isAdmin, 
            boolean isManager);

    int countUsers(UserSearchConditionDTO conditionDTO);

    List<UserVO> searchUsers(UserSearchConditionDTO conditionDTO);

    void userInfoDownload(
            UserSearchConditionDTO conditionDTO, 
            HttpServletResponse response
        ) throws IOException;

}   
