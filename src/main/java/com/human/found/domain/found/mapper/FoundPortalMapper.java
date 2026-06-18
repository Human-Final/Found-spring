package com.human.found.domain.found.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.found.vo.FoundVO;

@Mapper
public interface FoundPortalMapper {
    int existsByAtcId(String atcId);
    int insertFoundPortal(FoundVO foundVO);
}
