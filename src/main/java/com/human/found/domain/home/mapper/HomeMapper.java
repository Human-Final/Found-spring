package com.human.found.domain.home.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.lost.vo.LostVO;

@Mapper
public interface HomeMapper {

    // 메인 화면에 최신 습득/분실물 2개씩 출력
    public List<FoundVO> selectRecentFoundList();
    public List<LostVO> selectRecentLostList();

    // 메인 화면에 습득/분실/완료 카운트
    public long countWeeklyFound();
    public long countWeeklyLost();
    public long countWeeklyDone();

}