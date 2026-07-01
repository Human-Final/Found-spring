package com.human.found.domain.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminNoticeVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

@Mapper
public interface AdminMapper {

    // 사용자 등록 분실물 선택 삭제
    int deleteLostList(@Param("nums") List<Long> nums);

    // 경찰청 API 분실물 선택 삭제
    int deletePoliceLostList(@Param("nums") List<Long> nums);

    // 사용자 등록 습득물 선택 삭제
    int deleteFoundList(@Param("nums") List<Long> nums);

    // 경찰청 API 습득물 선택 삭제
    int deletePoliceFoundList(@Param("nums") List<Long> nums);

    // 관리자 등록 공지사항 선택 삭제
    int deleteNoticeList(@Param("nums") List<Long> nums);

    // 관리자 분실물 검색 + 페이징 조회
    List<AdminLostVO> searchLostPage(AdminSearchVO searchVO);

    // 관리자 분실물 검색 결과 개수 조회
    int countSearchLost(AdminSearchVO searchVO);

    // 관리자 습득물 검색 + 페이징 조회
    List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO);

    // 관리자 습득물 검색 결과 개수 조회
    int countSearchFound(AdminSearchVO searchVO);

    // 관리자 공지사항 검색 + 페이징 조회
    List<AdminNoticeVO> searchNoticePage(AdminSearchVO searchVO);

    // 관리자 공지사항 검색 결과 개수 조회
    int countSearchNotice(AdminSearchVO searchVO);
}