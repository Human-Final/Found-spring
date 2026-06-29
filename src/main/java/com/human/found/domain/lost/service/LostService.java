package com.human.found.domain.lost.service;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.vo.LostVO;

public interface LostService {
    public void LostRegister(LostVO lostVO,MultipartFile[]files);
    public List<LostVO> getLostlist();
    //삭제
    public void deletelost(String inputpw , String atcId ,String loginid,boolean isAdmin);
    
    public LostVO lostdetail(String atcId);

    // 게시글 번호로 분실물 조회
    LostVO getLostByNum(Long num);

    public void UpdateLost(LostVO lostVO,MultipartFile[] files,List<String>deletefiles);

    // 분실물 카테고리별 검색하기
    public List<LostVO> searchLostItems(
        List<String> category, 
        String subCategory, 
        String startDate, 
        String endDate, 
        String author, 
        String status, 
        String keyword, 
        String sort
    );


}
