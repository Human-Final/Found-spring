package com.human.found.domain.lost.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.human.found.domain.lost.mapper.LostMapper;
import com.human.found.domain.lost.vo.LostVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LostServiceImpl implements LostService {
    private final LostMapper lostMapper;

    @Override
    public void LostRegister(LostVO lostVO) {
        lostMapper.insertLost(lostVO);
    }

    @Override
    public List<LostVO> getLostlist() {
       return lostMapper.selectLostList();
    }

}
