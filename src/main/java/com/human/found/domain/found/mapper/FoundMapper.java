package com.human.found.domain.found.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundVO;

@Mapper
public interface FoundMapper {
    public void insertfound(FoundVO foundVO);
    public List<FoundVO> selectFoundList();
    public void FoundupdateDelete(@Param("foundNum") Long foundNum);
    public FoundVO SelectFoundById(@Param("foundNum") Long foundNum );
}
