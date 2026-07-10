package com.human.found.domain.comment.service;

import java.util.List;

import com.human.found.domain.comment.vo.CommentVO;

public interface LostCommentService {

    // 댓글 목록 조회
    List<CommentVO> getComments(Long num, String dataSource);

    // 댓글 등록
    void addComment(CommentVO comment);

    // 댓글 단건 조회
    CommentVO getCommentByCommentNum(Long commentNum);

    // 댓글 수정
    void updateComment(CommentVO comment);

    // 댓글 삭제
    void deleteComment(Long commentNum);

    // 유저의 이메일 알아내기
    String findUserEmailByAtcId(String atcId);

    // 유저에게 이메일 보내기
    String emailNotify(String userEmail, String atcId);
}
