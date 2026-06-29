package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.user.vo.UserVO;

public interface UserManageService {
    int updateUserStatusByIds(List<String> userIds, String status, int isDeleted);

    List<UserVO> totalUserList();
}
