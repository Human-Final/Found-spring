package com.human.found.domain.found.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.vo.FoundVO;


public interface FoundService {
    
    //등록
    public void Register(FoundVO foundVO,MultipartFile[]files);
    
    //조회
    public List<FoundVO> getFoundList();

    //삭제
    public void deletefound(Long foundNum,String inputpw,String loginid);


}
