package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.user.vo.UserVO;

public interface UserManageService {
    
    List<UserVO> totalUserList();

    int updateUserBulk(
            List<String> statusUserIds, 
            List<String> statuses, 
            List<Integer> isDeletedList,
            List<String> roleUserIds, 
            List<String> roles, 
            boolean isAdmin, 
            boolean isManager);
}
