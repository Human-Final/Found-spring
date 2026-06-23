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
}
