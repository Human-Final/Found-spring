package com.human.found.domain.found.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.found.vo.FoundVO;

@Mapper
public interface FoundPoliceMapper {

    // atc_id 중복 확인
    int existsByAtcId(String atcId);

    // 경찰청 습득물 DB 저장
    int insertFoundPolice(FoundVO foundVO);
}