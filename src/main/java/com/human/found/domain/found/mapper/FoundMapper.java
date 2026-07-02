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

    // 검색용
    List<FoundVO> selectFoundSearchList(
        @Param("category") List<String> category, 
        @Param("subCategory") String subCategory, 
        @Param("colorSelect") String colorSelect, 
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate, 
        @Param("author") String author, 
        @Param("status") String status, 
        @Param("keyword") String keyword, 
        @Param("sort") String sort,
        @Param("paging") PagingVO pagingVO
    );

    // 조건에 부합하는 검색결과 조회 갯수 확인용
    int selectFoundSearchCount(
        @Param("category") List<String> category, 
        @Param("subCategory") String subCategory,
        @Param("colorSelect") String colorSelect,
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate, 
        @Param("author") String author, 
        @Param("status") String status, 
        @Param("keyword") String keyword
    );
    
    //글 확인용
    public FoundVO selectFoundById(@Param("atcId") String atcId );
    public FoundVO selectDetailatcId(@Param("atcId") String atcId);
    public long countFoundList();

    //외부 api
    void PoliceDelete(@Param("atcId") String atcId);
    void PortalDelete(@Param("atcId") String atcId);

    // 댓글
    FoundVO getFoundByNum(Long num);

    //게시글 수정
    void updateFound(FoundVO foundVO);

    //test
    
    void updateThumbnail(FoundVO foundVO);
    
}
