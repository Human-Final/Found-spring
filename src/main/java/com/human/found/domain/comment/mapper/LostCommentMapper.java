package com.human.found.domain.comment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.comment.vo.CommentVO;

@Mapper
public interface LostCommentMapper {
    
    List<CommentVO> findComments(@Param("num") Long num,
                                        @Param("dataSource") String dataSource);

    // 댓글 등록
    void insertComment(CommentVO comment);

    // 댓글 단건 조회
    CommentVO findByCommentNum(Long commentNum);

    // 댓글 수정
    void updateComment(CommentVO comment);

    // 분실물 댓글 1개삭제
    void deleteComment(@Param("commentNum") Long commentNum);
}
