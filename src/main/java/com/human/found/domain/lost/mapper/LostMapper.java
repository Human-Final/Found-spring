package com.human.found.domain.lost.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.lost.vo.LostVO;

@Mapper

public interface LostMapper {
    public void insertLost(LostVO lostVO);
    
    public List<LostVO> selectLostList();
    
    public LostVO selectlostbyId(@Param("atcId")String atcId);
    
    //게시글 삭제
    public void lostupdateDelte(@Param("atcId")String atcId);

    
    public LostVO selectDetailAtcId(@Param("atcId")String atcId);

    //외부 api 삭제
    public void PoliceDelete(@Param("atcId")String atcId);
    
    LostVO findByNum(Long num);

    //이미지 저장
    void updateThumbnail(LostVO lostVO);

    void UpdateLost(LostVO lostVO);

    

}
