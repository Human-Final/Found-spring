package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;

public interface AdminService {
    
    // 사용자 등록 분실물 게시글 전체 조회
    List<AdminLostVO> getLostList();

    // 사용자 등록 분실물 게시글 삭제
    void deleteLost(Long num);

    // 사용자 분실물 게시글 선택 삭제
    void deleteLostList(List<Long> nums);

    // 경찰청 API 분실물 선택 삭제
    void deletePoliceLostList(List<Long> nums);

    // 사용자 등록 습득물 전체 조회
    List<AdminFoundVO> getFoundList();

    // 경찰청 API 습득물 전체 조회
    List<AdminFoundVO> getPoliceFoundList();

    // 사용자 등록 습득물 선택 삭제
    void deleteFoundList(List<Long> nums);

    // 경찰청 API 습득물 선택 삭제
    void deletePoliceFoundList(List<Long> nums);
}
