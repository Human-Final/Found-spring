package com.human.found.domain.lost.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.lost.vo.LostFileVO;

@Mapper
public interface LostFileMapper {
    public void insertFile(LostFileVO lostFileVO);
}
