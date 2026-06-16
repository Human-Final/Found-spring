package com.human.found.domain.lost.mapper;

import com.human.found.domain.lost.vo.LostPoliceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LostPoliceMapper {
    // 벌크 인서트 배치 쿼리
    int insertLostGoodsBatch(List<LostPoliceVO> lostList);
    
    // DB 백업본 스캔 쿼리
    List<LostPoliceVO> selectLostGoodsList();

    LostPoliceVO selectLostDetail(@Param("atcId") String atcId);

    
}