package com.human.found.domain.found.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.global.common.paging.PagingVO;


public interface FoundService {
    
    //등록
    public void Register(FoundVO foundVO,MultipartFile[]files);
    
    //조회
    public List<FoundVO> getFoundList(PagingVO pagingVO);

    //삭제
    public String deletefound(Long foundNum,String inputpw,String loginid);

    //상세보기
    public FoundVO foundgetdetail(String atcId);
    
    // 게시글 번호로 상세조회
    public FoundVO getFoundByNum(Long num);

}
