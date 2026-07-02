package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.user.vo.UserVO;

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

}
