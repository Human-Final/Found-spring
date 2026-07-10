package com.human.found.infrastructure.police.foundPolicePortal.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundVO;

@Mapper
public interface FoundPortalMapper {

    // 경찰청 습득물 batch upsert
    int upsertFoundPortal(@Param("list") List<FoundVO> list);

    // 포털기관 6개월 이전 데이터 논리삭제
    int markOldFoundPortalDeleted(@Param("baseDate") LocalDateTime baseDate);

    // int existsByAtcId(String atcId);
    // int deleteAllFoundPortal();
}
