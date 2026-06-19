package com.human.found.domain.lost.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.lost.vo.LostFileVO;

@Mapper
public interface LostFileMapper {
    public void insertFile(LostFileVO lostFileVO);
    public List<LostFileVO> findById(@Param("atcId")String atcId);
    // atc_id를 기준으로 파일 데이터를 DB에서 지우는 메서드
    public void deleteByAtcId(@Param("atcId")String atcId);
}
