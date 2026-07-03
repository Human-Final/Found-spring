package com.human.found.domain.admin.service;

import java.io.IOException;
import java.util.List;

import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserManageService {
    
    List<UserVO> totalUserList();

    int updateUserBulk(
            List<String> statusUserIds, 
            List<String> statuses, 
            List<Integer> isDeletedList,
            List<String> roleUserIds, 
            List<String> roles, 
            List<String> profileUserIds,
            List<String> names,
            List<String> emails,
            List<String> tels,
            boolean isAdmin, 
            boolean isManager);

    int countUsers(UserSearchConditionDTO conditionDTO);

    List<UserVO> searchUsers(UserSearchConditionDTO conditionDTO);

    void userInfoDownload(
            UserSearchConditionDTO conditionDTO, 
            HttpServletResponse response
        ) throws IOException;

}   
