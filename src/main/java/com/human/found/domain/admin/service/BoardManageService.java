package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

public interface BoardManageService {

    // 사용자 등록 분실물 선택 삭제
    void deleteLostList(List<String> atcId);

    // 사용자 등록 습득물 선택 삭제
    void deleteFoundList(List<String> atcId);

    // 관리자 분실물 검색 + 페이징 조회
    List<AdminLostVO> searchLostPage(AdminSearchVO searchVO);

    // 관리자 분실물 검색 결과 개수 조회
    int countSearchLost(AdminSearchVO searchVO);

    // 관리자 습득물 검색 + 페이징 조회
    List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO);

    // 관리자 습득물 검색 결과 개수 조회
    int countSearchFound(AdminSearchVO searchVO);

    // 검색 조건이 비어있는지 판단하는 메서드
    public boolean isSearchConditionEmpty(AdminSearchVO searchVO);

    // 관리자 분실물 완료처리
    public void completeLostList(List<String> atcId);

    // 관리자 유실물 완료처리
    public void completeFoundList(List<String> atcId);

}