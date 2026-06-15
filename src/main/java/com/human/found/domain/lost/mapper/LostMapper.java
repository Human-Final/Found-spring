package com.human.found.domain.lost.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.lost.vo.LostVO;

@Mapper

public interface LostMapper {
    public void insertLost(LostVO lostVO);
    public List<LostVO> selectLostList();
}
