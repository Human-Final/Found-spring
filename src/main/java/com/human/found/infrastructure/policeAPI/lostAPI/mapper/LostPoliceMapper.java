package com.human.found.infrastructure.policeAPI.lostAPI.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.lost.vo.LostVO;

@Mapper
public interface LostPoliceMapper {
    
    // 경찰청 분실물 정보 DB삽입
    int upsertLostPolice(List<LostVO> lostList);
    
    // 경찰청 분실물 정보 불러오기
    List<LostVO> selectLostPoliceList();

    // 경찰청 분실물 정보 자세히보기
    LostVO selectLostDetail(@Param("atcId") String atcId);

    // 경찰청 분실물 정보 전체 삭제
    void lostPoliceDelete();

    // 6개월 지난 경찰청 분실물 정보 삭제
    int softDeleteOldLostPolice(@Param("deleteThreshold") LocalDateTime deleteThreshold);
    
}