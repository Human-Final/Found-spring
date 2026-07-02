package com.human.found.domain.lost.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.global.common.paging.PagingVO;

@Mapper
public interface LostMapper {
    public void insertLost(LostVO lostVO);
    
    public List<LostVO> selectLostList(PagingVO pagingVO);
    
    public LostVO selectlostbyId(@Param("atcId")String atcId);
    
    //게시글 삭제
    public void lostupdateDelte(@Param("atcId")String atcId);

    public LostVO selectDetailAtcId(@Param("atcId") String atcId);

    // 외부 api 삭제
    public void PoliceDelete(@Param("atcId") String atcId);
    
    LostVO findByNum(Long num);

    // 이미지 저장
    void updateThumbnail(LostVO lostVO);

    void UpdateLost(LostVO lostVO);

    // 기존 메서드의 파라미터를 상세 검색용 변수로 확장 매핑합니다.
    List<LostVO> selectLostSearchList(
        @Param("category") List<String> category, 
        @Param("subCategory") String subCategory, 
        @Param("startDate") String startDate,   // 추가
        @Param("endDate") String endDate,       // 추가
        @Param("author") String author,         // 추가
        @Param("status") String status, 
        @Param("keyword") String keyword,       // 습득물명
        @Param("sort") String sort,
        @Param("paging") PagingVO pagingVO
    );

    int selectLostSearchCount(
        @Param("category") List<String> category, 
        @Param("subCategory") String subCategory, 
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate, 
        @Param("author") String author, 
        @Param("status") String status, 
        @Param("keyword") String keyword
    );

    long countLostList();

}
