package com.human.found.domain.lost.service;

import java.util.List;

import com.human.found.domain.lost.vo.LostVO;

public interface LostService {
    public void LostRegister(LostVO lostVO);
    public List<LostVO> getLostlist();
}
