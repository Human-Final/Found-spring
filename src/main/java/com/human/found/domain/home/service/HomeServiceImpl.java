package com.human.found.domain.home.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.home.mapper.HomeMapper;
import com.human.found.domain.lost.vo.LostVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService{
    private final HomeMapper homeMapper;
    
    // 습득물 최신 2개
    @Override
    public List<FoundVO> recentFoundList() {
       return homeMapper.selectRecentFoundList();
    }

    // 분실물 최신 2개
    @Override
    public List<LostVO> recentLostList() {
        return homeMapper.selectRecentLostList();
    }

    // 습득물 카운트(일주일)
    @Override
    public long countWeeklyFound() {
        return homeMapper.countWeeklyFound();
    }

    // 분실물 카운트(일주일)
    @Override
    public long countWeeklyLost() {
       return homeMapper.countWeeklyLost();
    }

    // 완료 카운트(일주일)
    @Override
    public long countWeeklyDone() {
        return homeMapper.countWeeklyDone();
    }

    @Override
    public long countTotalFound() {
       return homeMapper.countTotalFound();
    }

    @Override
    public long countTotalLost() {
       return homeMapper.countTotalLost();
    }


}
