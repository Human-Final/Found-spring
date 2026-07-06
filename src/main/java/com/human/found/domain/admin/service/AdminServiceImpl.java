package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.AdminMapper;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminNoticeVO;
import com.human.found.domain.admin.vo.AdminSearchVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminMapper adminMapper;

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

    // 사용자 등록 분실물 선택 삭제
    @Override
    @Transactional
    public void deleteLostList(List<Long> nums) {

        if(nums == null || nums.isEmpty()) {
            return;
        }

        adminMapper.deleteLostList(nums);
    }

    // 경찰청 API 분실물 선택 삭제
    @Override
    @Transactional
    public void deletePoliceLostList(List<Long> nums) {

        if(nums == null || nums.isEmpty()) {
            return;
        }

        adminMapper.deletePoliceLostList(nums);
    }

    /**
     * 사용자 등록 습득물 게시글 선택 삭제
     * - 체크박스로 선택한 게시글들을 논리 삭제
     */
    @Override
    @Transactional
    public void deleteFoundList(List<Long> nums) {

        // 선택된 게시글이 없는 경우 종료
        if (nums == null || nums.isEmpty()) {
            return;
        }

        // 선택된 게시글 논리 삭제
        adminMapper.deleteFoundList(nums);
    }

    /**
     * 경찰청 API 습득물 게시글 선택 삭제
     * - 체크박스로 선택한 게시글들을 논리 삭제
     */
    @Override
    @Transactional
    public void deletePoliceFoundList(List<Long> nums) {

        // 선택된 게시글이 없는 경우 종료
        if (nums == null || nums.isEmpty()) {
            return;
        }

        // 선택된 게시글 논리 삭제
        adminMapper.deletePoliceFoundList(nums);
    }

    // 관리자 등록 공지사항 선택 삭제
    @Override
    @Transactional
    public void deleteNoticeList(List<Long> nums) {

        // 선택된 공지사항이 없는 경우 종료
        if (nums == null || nums.isEmpty()) {
            return;
        }

        // 선택된 공지사항 논리 삭제
        adminMapper.deleteNoticeList(nums);
    }

    // 관리자 분실물 검색 + 페이징 조회
    @Override
    public List<AdminLostVO> searchLostPage(AdminSearchVO searchVO) {
        return adminMapper.searchLostPage(searchVO);
    }

    // 관리자 분실물 검색 결과 개수 조회
    @Override
    public int countSearchLost(AdminSearchVO searchVO) {
        return adminMapper.countSearchLost(searchVO);
    }

    // 관리자 습득물 검색 + 페이징 조회
    @Override
    public List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO) {
        return adminMapper.searchFoundPage(searchVO);
    }

    // 관리자 습득물 검색 결과 개수 조회
    @Override
    public int countSearchFound(AdminSearchVO searchVO) {
        return adminMapper.countSearchFound(searchVO);
    }

    // 관리자 공지사항 검색 + 페이징 조회
    @Override
    public List<AdminNoticeVO> searchNoticePage(AdminSearchVO searchVO) {
        return adminMapper.searchNoticePage(searchVO);
    }

    // 관리자 공지사항 검색 결과 개수 조회
    @Override
    public int countSearchNotice(AdminSearchVO searchVO) {
        return adminMapper.countSearchNotice(searchVO);
    }
}