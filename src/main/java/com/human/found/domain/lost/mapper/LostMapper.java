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
    
    public LostVO selectlostbyId(@Param("lostNum")Long lostNum);
    
    public void lostupdateDelte(@Param("lostNum")Long lostNum);

    public LostVO selectDetailAtcId(@Param("atcId")String atcId);

}
