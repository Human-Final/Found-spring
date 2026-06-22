package com.human.found.domain.found.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.global.common.paging.PagingVO;

@Mapper
public interface FoundMapper {
    public void insertfound(FoundVO foundVO);
    public List<FoundVO> selectFoundList(PagingVO pagingVO);
    public void FoundupdateDelete(@Param("foundNum") Long foundNum);
    //글 확인용
    public FoundVO SelectFoundById(@Param("foundNum") Long foundNum );
    public FoundVO SelectDetailatcID(@Param("atcId") String atcId);
    public long countFoundList();

    // 댓글
    FoundVO getFoundByNum(Long num);
}
