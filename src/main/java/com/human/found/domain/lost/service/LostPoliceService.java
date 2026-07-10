package com.human.found.domain.lost.service;

import java.util.List;

import com.human.found.domain.lost.vo.LostVO;

public interface LostPoliceService {
    
    public void savePoliceLost(int pageNo, int numOfRows);

    public List<LostVO> getLostGoodsFromDB();

    public LostVO getDetailByAtcId(String atcId);

}
