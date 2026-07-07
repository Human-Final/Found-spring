package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.BoardManageMapper;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardManageServiceImpl implements BoardManageService{

    private final BoardManageMapper boardManageMapper;

    public boolean isSearchConditionEmpty(AdminSearchVO searchVO) {
        // 1. 검색어가 있는지 확인
        boolean hasKeyword = searchVO.getKeyword() != null && !searchVO.getKeyword().trim().isEmpty();
        
        // 2. 카테고리가 선택되었는지 확인 (체크박스 다중 선택이면 보통 List나 배열 형태입니다)
        // 예: searchVO.getCategories() 또는 변수명에 맞게 매칭하세요.
        boolean hasCategory = searchVO.getCategories() != null && !searchVO.getCategories().isEmpty();
        
        // 3. 상태(진행중/완료)가 선택되었는지 확인
        boolean hasStatus = searchVO.getDoneList() != null && !searchVO.getDoneList().isEmpty();
        
        // 4. 데이터출처(사용자/경찰/포털)가 선택되었는지 확인
        boolean hasSource = searchVO.getDataSources() != null && !searchVO.getDataSources().isEmpty();

        // 5. 등록일 날짜 지정이 되어있는지 확인 (예: startRegDate, endRegDate 등)
        boolean hasStartDate = searchVO.getStartDate() != null && !searchVO.getStartDate().trim().isEmpty();
        boolean hasEndDate = searchVO.getEndDate() != null && !searchVO.getEndDate().trim().isEmpty();

        // 6. 삭제 포함 체크박스가 체크 되었는지 확인
        boolean hasIncludeDeleted = searchVO.isIncludeDeleted();

        // 모든 조건이 다 false(없음)여야 "비어있는 최초 상태"입니다.
        // 하나라도 true가 있다면 사용자가 검색 조건을 넣은 것이므로 false를 반환합니다.
        return !(hasKeyword || hasCategory || hasStatus || hasSource || hasStartDate || hasEndDate || hasIncludeDeleted);
    }

    // 관리자 모드 분실물 선택 삭제
    @Override
    @Transactional
    public void deleteLostList(List<String> atcId) {

        boardManageMapper.deleteLostList(atcId);
    }

    // 관리자 모드 습득물 선택 삭제
    @Override
    @Transactional
    public void deleteFoundList(List<String> atcId) {
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.deleteFoundList(id);
            }
            else if(id.startsWith("F")){
                boardManageMapper.deleteFoundPoliceList(id);
            }
            else{
                boardManageMapper.deleteFoundPortalList(id);
            }
        }
    }

    // 관리자 분실물 완료 처리
    @Override
    public void completeLostList(List<String> atcId){
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.completeLostList(id);
            }
            else{
                boardManageMapper.completeLostPoliceList(id);
            }
        }
    }

    // 관리자 습득물 완료 처리
    @Override
    public void completeFoundList(List<String> atcId){
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.completeFoundList(id);
            }
            else if(id.startsWith("F")){
                boardManageMapper.completeFoundPoliceList(id);
            }
            else{
                boardManageMapper.completeFoundPortalList(id);
            }
        }
    }


    // 관리자 분실물 검색 + 페이징 조회
    @Override
    public List<AdminLostVO> searchLostPage(AdminSearchVO searchVO) {
        return boardManageMapper.searchLostPage(searchVO);
    }

    // 관리자 분실물 검색 결과 개수 조회
    @Override
    public int countSearchLost(AdminSearchVO searchVO) {
        return boardManageMapper.countSearchLost(searchVO);
    }

    // 관리자 습득물 검색 + 페이징 조회
    @Override
    public List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO) {
        return boardManageMapper.searchFoundPage(searchVO);
    }

    // 관리자 습득물 검색 결과 개수 조회
    @Override
    public int countSearchFound(AdminSearchVO searchVO) {
        return boardManageMapper.countSearchFound(searchVO);
    }

}