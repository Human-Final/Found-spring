package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.AdminMapper;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminNoticeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminMapper adminMapper;

    // 분실물 게시글 전체 조회
    @Override
    public List<AdminLostVO> getLostList() {
        return adminMapper.selectLostList();
    }

    // 경찰청 API 분실물 전체 조회
    @Override
    public List<AdminLostVO> getPoliceLostList() {
        return adminMapper.selectPoliceLostList();
    }

    // 분실물 선택 삭제
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
    
    // 사용자 등록 습득물 게시글 전체 조회
    @Override
    public List<AdminFoundVO> getFoundList() {
        return adminMapper.selectFoundList();
    }

    //경찰청 API 습득물 게시글 전체 조회
    @Override
    public List<AdminFoundVO> getPoliceFoundList() {
        return adminMapper.selectPoliceFoundList();
    }

    /**
     * 사용자 등록 습득물 게시글 선택 삭제
     * - 체크박스로 선택한 게시글들을 논리 삭제
     */
    @Override
    @Transactional // DB 데이터 변경으로 인해 트랜잭션 적용
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
    @Transactional // DB 데이터 변경으로 인해 트랜잭션 적용
    public void deletePoliceFoundList(List<Long> nums) {

        // 선택된 게시글이 없는 경우 종료
        if (nums == null || nums.isEmpty()) {
            return;
        }

        // 선택된 게시글 논리 삭제
        adminMapper.deletePoliceFoundList(nums);
    }

    // 관리자 등록 공지사항 전체 조회
    @Override
    public List<AdminNoticeVO> getNoticeList() {
        return adminMapper.selectNoticeList();
    }

    // 관리자 등록 공지사항 선택 삭제
    @Override
    public void deleteNoticeList(List<Long> nums) {
        
        // 선택된 공지사항이 없는 경우 종료
        if (nums == null || nums.isEmpty()) {
            return;
        }

        // 선택된 공지사항 논리 삭제
        adminMapper.deleteNoticeList(nums);
    }

    // 사용자 등록 분실물 게시글 페이징 조회
    @Override
    public List<AdminLostVO> getLostPage(int page, int size) {
        int offset = (page - 1) * size;
        return adminMapper.selectLostPage(offset, size);
    }

    // 경찰청 API 분실물 페이징 조회
    @Override
    public List<AdminLostVO> getPoliceLostPage(int page, int size) {
        int offset = (page - 1) * size;
        return adminMapper.selectPoliceLostPage(offset, size);
    }

    // 사용자 등록 습득물 페이징 조회
    @Override
    public List<AdminFoundVO> getFoundPage(int page, int size) {
        int offset = (page - 1) * size;
        return adminMapper.selectFoundPage(offset, size);
    }

    // 경찰청 API 습득물 페이징 조회
    @Override
    public List<AdminFoundVO> getPoliceFoundPage(int page, int size) {
        int offset = (page - 1) * size;
        return adminMapper.selectPoliceFoundPage(offset, size);
    }

    // 관리자 등록 공지사항 페이징 조회
    @Override
    public List<AdminNoticeVO> getNoticePage(int page, int size) {
        int offset = (page - 1) * size;
        return adminMapper.selectNoticePage(offset, size);
    }

    // 사용자 등록 분실물 전체 개수 조회
    @Override
    public int countLost() {
        return adminMapper.countLost();
    }

    // 경찰청 API 분실물 전체 개수 조회
    @Override
    public int countPoliceLost() {
        return adminMapper.countPoliceLost();
    }

    // 사용자 등록 습득물 전체 개수 조회
    @Override
    public int countFound() {
        return adminMapper.countFound();
    }

    // 경찰청 API 습득물 전체 개수 조회
    @Override
    public int countPoliceFound() {
        return adminMapper.countPoliceFound();
    }

    // 관리자 등록 공지사항 전체 개수 조회
    @Override
    public int countNotice() {
        return adminMapper.countNotice();
    }
}