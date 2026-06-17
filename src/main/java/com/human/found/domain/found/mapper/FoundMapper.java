package com.human.found.domain.found.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.found.vo.FoundVO;

@Mapper
public interface FoundMapper {
    public void insertfound(FoundVO foundVO);
    public List<FoundVO> selectFoundList();
}
