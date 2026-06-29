package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.AdminMapper;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;

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

    // 분실물 게시글 삭제
    @Override
    @Transactional // DB 변화 때문에 적용
    public void deleteLost(Long num) {
        adminMapper.deleteLost(num);
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
}
