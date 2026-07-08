package com.human.found.domain.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

@Mapper
public interface BoardManageMapper {

    // 사용자 등록 분실물 선택 삭제
    void deleteLostList(@Param("atcId") List<String> atcId);

    // 사용자 등록 습득물 선택 삭제
    void deleteFoundList(String atcId);

    // 경찰청 습득물 삭제
    void deleteFoundPoliceList(String atcId);

    // 포털 습득물 삭제
    void deleteFoundPortalList(String atcId);

    // 사용자 등록 분실물 완료 처리
    void completeLostList(String atcId);

    // 경찰청 분실물 완료 처리
    void completeLostPoliceList( String atcId);

    // 사용자 등록 습득물 완료 처리
    void completeFoundList(String atcId);

    // 경찰청 습득물 완료 처리
    void completeFoundPoliceList(String atcId);

    // 포털 습득물 완료 처리
    void completeFoundPortalList(String atcId);

    // 관리자 분실물 검색 + 페이징 조회
    List<AdminLostVO> searchLostPage(AdminSearchVO searchVO);

    // 관리자 분실물 검색 결과 개수 조회
    int countSearchLost(AdminSearchVO searchVO);

    // 관리자 습득물 검색 + 페이징 조회
    List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO);

    // 관리자 습득물 검색 결과 개수 조회
    int countSearchFound(AdminSearchVO searchVO);

}