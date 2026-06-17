package com.human.found.domain.found.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.vo.FoundVO;


public interface FoundService {
   
    public void Register(FoundVO foundVO,MultipartFile[]files);
    
    public List<FoundVO> getFoundList();

}
