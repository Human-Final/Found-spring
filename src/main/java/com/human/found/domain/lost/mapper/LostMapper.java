package com.human.found.domain.lost.mapper;

import com.human.found.domain.lost.vo.LostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LostMapper {
    // 벌크 인서트 배치 쿼리
    int insertLostGoodsBatch(List<LostVO> lostList);
    
    // DB 백업본 스캔 쿼리
    List<LostVO> selectLostGoodsList();

    LostVO selectLostDetail(@Param("atcId") String atcId);

    
}