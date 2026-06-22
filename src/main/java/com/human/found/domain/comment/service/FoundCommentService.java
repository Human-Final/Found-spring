package com.human.found.domain.comment.service;

import java.util.List;

import com.human.found.domain.comment.vo.FoundCommentVO;

public interface FoundCommentService {
    
    // 특정 습득 게시글 댓글 목록 조회
    List<FoundCommentVO> getCommentsByNum(Long num);

    // 댓글 등록
    int insertComment(FoundCommentVO comentVO);

    // 댓글 단건 조회
    FoundCommentVO getCommentByCommentNum(Long commentNum);

    // 댓글 수정
    int updateComment(FoundCommentVO commentVO);
    
    // 댓글 삭제
    int deleteComment(Long commentNum);
}
