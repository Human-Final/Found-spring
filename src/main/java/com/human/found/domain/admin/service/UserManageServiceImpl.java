package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.UserManageMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService{
    
    private final UserManageMapper userManageMapper;

    // 유저 상태 변경
    @Transactional
    public int updateUserStatusByIds(List<String> userIds, String status, int isDeleted){
        if (userIds == null || userIds.isEmpty()){
            throw new IllegalArgumentException("선택된 회원이 없습니다.");
        }

        return userManageMapper.updateUserStatusByIds(userIds, status, isDeleted);
    }

    // 유저 전체 조회
    public List<UserVO> totalUserList(){
        return userManageMapper.totalUserList();
    }
}
