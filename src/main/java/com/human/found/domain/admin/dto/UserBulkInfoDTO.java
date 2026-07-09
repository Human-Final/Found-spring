package com.human.found.domain.admin.dto;

import java.util.ArrayList;
import java.util.List;

import com.human.found.domain.user.vo.UserVO;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBulkInfoDTO {
    
    // 기존 회원 수정
    @Valid
    private List<UserVO> changedUsers = new ArrayList<>();
    
    // 신규 회원 추가
    @Valid
    private List<UserVO> newUsers = new ArrayList<>();
}
