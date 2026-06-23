package com.human.found.domain.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.comment.mapper.LostCommentMapper;
import com.human.found.domain.comment.vo.LostCommentVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LostCommentServiceImpl implements LostCommentService{

    private final LostCommentMapper lostCommentMapper;

    // 댓글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<LostCommentVO> getComments(Long num, String dataSource) {
        return lostCommentMapper.findComments(num, dataSource);
    }

    // 댓글 등록
    @Override
    public void addComment(LostCommentVO comment) {
        lostCommentMapper.insertComment(comment);
    }

    // 댓글 단건 조회
    @Override
    public LostCommentVO getComment(Long commentNum) {
        return lostCommentMapper.findByCommentNum(commentNum);
    }

    // 댓글 수정
    @Override
    public void updateComment(LostCommentVO comment) {
        lostCommentMapper.updateComment(comment);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Long commentNum, String id) {
        lostCommentMapper.deleteComment(commentNum, id);
    }
    
}
