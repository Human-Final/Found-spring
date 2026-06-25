package com.human.found.domain.comment.service;

import java.util.List;

import com.human.found.domain.comment.vo.CommentVO;

public interface FoundCommentService {
    
    // 특정 습득 게시글 댓글 목록 조회
    List<CommentVO> getCommentsByNum(Long num, String dataSource);

    // 댓글 등록
    int insertComment(CommentVO comentVO);

    // 댓글 단건 조회
    CommentVO getCommentByCommentNum(Long commentNum);

    // 댓글 수정
    int updateComment(CommentVO commentVO);
    
    // 댓글 삭제
    void deleteComment(Long commentNum);
}
