package com.human.found.domain.found.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundFileVO;

@Mapper
public interface FoundFileMapper {
    public void insertFile(FoundFileVO foundFileVO);
    List<FoundFileVO>findById(@Param("atcId") String atcId);
}
