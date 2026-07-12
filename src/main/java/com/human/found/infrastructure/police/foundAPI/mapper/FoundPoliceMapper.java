package com.human.found.infrastructure.police.foundAPI.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundVO;

/**
 * 경찰청 습득물 API DB Mapper
 *
 * 역할
 * 1. 경찰청 API에서 받아온 습득물 중복 여부 확인
 * 2. 새로운 습득물 DB 저장
 * 3. 경찰청 습득물 전체 삭제
 */
@Mapper
public interface FoundPoliceMapper {

    /**
     * atc_id(물품 고유번호) 중복 확인
     *
     * @param atcId 경찰청 물품 고유번호
     * @return 존재하면 1 이상, 없으면 0
     */
    int existsByAtcId(@Param("atcId") String atcId);

    /**
     * 경찰청 습득물 저장
     *
     * @param foundVO 저장할 습득물 정보
     * @return 저장 성공 행 개수
     */
    int insertFoundPolice(FoundVO foundVO);

    /**
     * 경찰청 습득물 전체 삭제
     */
    void deleteAllFoundPolice();

}