package com.human.found.domain.admin.dto;

import java.util.ArrayList;
import java.util.List;

import com.human.found.domain.user.vo.UserVO;
import com.human.found.global.common.paging.PagingVO;

import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class UserSearchConditionDTO extends PagingVO{
    
    // 아이디, 이름, 이메일 통합 검색
    private String keyword;

    // 회원 상태 다중 선택
    // 체크박스를 아무것도 선택하지 않아도 기본값이 빈 리스트일 수 있도록 
    // 여기서 미리 생성(초기화)해서 null 값을 방어
    private List<String> statuses = new ArrayList<>();
    private boolean includeDeleted;      // deleted 포함 여부

    // 권한 다중 선택
    private List<String> roles = new ArrayList<>();

    // 가입일 검색
    private String startDate;
    private String endDate;

    // 신규 회원 추가를 위한 리스트
    private List<UserVO> newUsers = new ArrayList<>();


    // 상태에 탈퇴를 포함시키기 위해 세터를 따로 만들 필요가 있음 -> @Setter 사용 안함
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = new ArrayList<>();
        this.includeDeleted = false;

        if (statuses == null) {
            return;
        }

        for (String status : statuses) {
            if ("deleted".equals(status)) {
                this.includeDeleted = true;
            } else {
                this.statuses.add(status);
            }
        }
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? roles : new ArrayList<>();
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
