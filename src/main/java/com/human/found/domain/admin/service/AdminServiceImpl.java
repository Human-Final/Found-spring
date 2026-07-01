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