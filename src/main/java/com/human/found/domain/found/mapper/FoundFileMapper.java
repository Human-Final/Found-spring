package com.human.found.domain.found.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.found.vo.FoundFileVO;

@Mapper
public interface FoundFileMapper {
    public void insertFile(FoundFileVO foundFileVO);
}
