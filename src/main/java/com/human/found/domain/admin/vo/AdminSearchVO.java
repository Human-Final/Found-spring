package com.human.found.domain.admin.vo;

import java.util.List;

import com.human.found.global.common.paging.PagingVO;

import lombok.Data;

@Data
public class AdminSearchVO extends PagingVO {

    // 게시판 구분: lost / found / notice
    private String boardType;

    // 검색어
    private String keyword;

    // 카테고리
    private List<String> categories;

    // 상태: 0 진행중, 1 완료
    private List<Integer> doneList;

    // 시작일
    private String startDate;

    // 종료일
    private String endDate;

    // 데이터 출처: USER / POLICE / ADMIN
    private List<String> dataSources;

    // 게시글 데이터 삭제 게시글 포함 검색여부 확인
    private boolean includeDeleted;
}