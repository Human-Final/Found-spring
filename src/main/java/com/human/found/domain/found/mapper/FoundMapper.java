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
    public void FoundupdateDelete(@Param("atcId") String atcId );
    //글 확인용
    public FoundVO selectFoundById(@Param("atcId") String atcId );
    public FoundVO selectDetailatcId(@Param("atcId") String atcId);
    public long countFoundList();

    //외부 api
    void PoliceDelete(@Param("atcId") String atcId);
    void PortalDelete(@Param("atcId") String atcId);
}
