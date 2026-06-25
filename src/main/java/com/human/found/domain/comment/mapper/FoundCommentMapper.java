package com.human.found.domain.comment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.comment.vo.CommentVO;

@Mapper
public interface FoundCommentMapper {

    // 특정 습득 게시글 댓글 목록 조회
    List<CommentVO> selectCommentsByNum(
            @Param("num") Long num,
            @Param("dataSource") String dataSource
    );

    // 댓글 등록
    int insertComment(CommentVO commentVO);

    // 댓글 단건 조회
    CommentVO selectCommentByCommentNum(@Param("commentNum") Long commentNum);

    // 댓글 수정
    int updateComment(CommentVO commentVO);

    // 습득물 댓글 1개 삭제
    int deleteComment(@Param("commentNum") Long commentNum);
    
}
