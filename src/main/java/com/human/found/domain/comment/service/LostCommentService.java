package com.human.found.domain.comment.service;

import java.util.List;

import com.human.found.domain.comment.vo.LostCommentVO;

public interface LostCommentService {

    // 댓글 목록 조회
    List<LostCommentVO> getComments(Long num, String dataSource);

    // 댓글 등록
    void addComment(LostCommentVO comment);

    // 댓글 단건 조회
    LostCommentVO getComment(Long commentNum);

    // 댓글 수정
    void updateComment(LostCommentVO comment);

    // 댓글 삭제
    void deleteComment(Long commentNum, String id);
    
}
