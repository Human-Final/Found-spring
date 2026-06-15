package com.human.found.domain.found.servuce;

import java.util.List;

import com.human.found.domain.found.vo.FoundVO;


public interface FoundService {
   
    public void Register(FoundVO foundVO);
    
    public List<FoundVO> getFoundList();

}
