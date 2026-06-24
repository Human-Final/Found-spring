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

    // 댓글 삭제 처리
    void deleteComment(@Param("commentNum") Long commentNum,
                        @Param("id") String id);
}
