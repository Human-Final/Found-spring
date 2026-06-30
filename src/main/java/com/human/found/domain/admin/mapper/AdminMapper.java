package com.human.found.domain.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminNoticeVO;

@Mapper
public interface AdminMapper {

    // 사용자 등록 분실물 게시글 전체 조회
    List<AdminLostVO> selectLostList();

    // 경찰청 API 분실물 게시글 전체 조회
    List<AdminLostVO> selectPoliceLostList();

    // 사용자 등록 분실물 선택 삭제
    int deleteLostList(@Param("nums") List<Long> nums);

    // 경찰청 API 분실물 선택 삭제
    int deletePoliceLostList(@Param("nums") List<Long> nums);

    // 사용자 등록 습득물 게시글 전체 조회
    List<AdminFoundVO> selectFoundList();

    // 경찰청 API 습득물 게시글 전체 조회
    List<AdminFoundVO> selectPoliceFoundList();

    // 사용자 등록 습득물 선택 삭제
    int deleteFoundList(@Param("nums") List<Long> nums);

    // 경찰청 API 습득물 선택 삭제
    int deletePoliceFoundList(@Param("nums") List<Long> nums);

    // 관리자 등록 공지사항 전체 조회
    List<AdminNoticeVO> selectNoticeList();

    // 관리자 등록 공지사항 선택 삭제
    int deleteNoticeList(@Param("nums") List<Long> nums);
}