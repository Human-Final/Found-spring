package com.human.found.domain.found.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.found.vo.FoundFileVO;

@Mapper
public interface FoundFileMapper {
    public void insertFile(FoundFileVO foundFileVO);
    List<FoundFileVO>findById(@Param("atcId") String atcId);
    // atc_id를 기준으로 파일 데이터를 DB에서 지우는 메서드
    void deleteByAtcId(@Param("atcId") String atcId);

    // 게시판 수정 파일 삭제
    void deleteBySaveName(@Param("saveName")String saveName);

    
}
