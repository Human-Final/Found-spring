package com.human.found.domain.lost.service;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.vo.LostVO;

public interface LostService {
    public void LostRegister(LostVO lostVO,MultipartFile[]files);
    public List<LostVO> getLostlist();
    public void deletelost(String inputpw , Long lostNum ,String loginid);
}
