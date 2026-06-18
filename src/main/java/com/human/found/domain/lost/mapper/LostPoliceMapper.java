package com.human.found.domain.lost.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.lost.vo.LostVO;

@Mapper
public interface LostPoliceMapper {
    // 벌크 인서트 배치 쿼리
    int insertLostPolice(List<LostVO> lostList);
    
    // DB 백업본 스캔 쿼리
    List<LostVO> selectLostGoodsList();

    LostVO selectLostDetail(@Param("atcId") String atcId);

    void lostPoliceDelete();

    
}