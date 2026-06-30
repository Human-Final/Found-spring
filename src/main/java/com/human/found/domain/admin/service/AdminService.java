package com.human.found.domain.admin.service;

import java.util.List;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminNoticeVO;

public interface AdminService {
    
    // 사용자 등록 분실물 게시글 전체 조회
    List<AdminLostVO> getLostList();

    // 경찰청 API 분실물 전체 조회
    List<AdminLostVO> getPoliceLostList();

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

    // 관리자 등록 공지사항 전체 조회
    List<AdminNoticeVO> getNoticeList(); 

    // 관리자 등록 공지사항 선택 삭제
    void deleteNoticeList(List<Long> nums);

    // 사용자 등록 분실물 게시글 페이징 조회
    List<AdminLostVO> getLostPage(int page, int size);

    // 경찰청 API 분실물 페이징 조회
    List<AdminLostVO> getPoliceLostPage(int page, int size);

    // 사용자 등록 습득물 페이징 조회
    List<AdminFoundVO> getFoundPage(int page, int size);

    // 경찰청 API 습득물 페이징 조회
    List<AdminFoundVO> getPoliceFoundPage(int page, int size);

    // 관리자 등록 공지사항 페이징 조회
    List<AdminNoticeVO> getNoticePage(int page, int size);

    // 사용자 등록 분실물 전체 개수 조회
    int countLost();

    // 경찰청 API 분실물 전체 개수 조회
    int countPoliceLost();

    // 사용자 등록 습득물 전체 개수 조회
    int countFound();

    // 경찰청 API 습득물 전체 개수 조회
    int countPoliceFound();

    // 관리자 등록 공지사항 전체 개수 조회
    int countNotice();
}