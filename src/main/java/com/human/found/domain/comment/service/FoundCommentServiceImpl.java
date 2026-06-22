package com.human.found.domain.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.human.found.domain.comment.mapper.FoundCommentMapper;
import com.human.found.domain.comment.vo.FoundCommentVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundCommentServiceImpl implements FoundCommentService{

    private final FoundCommentMapper foundCommentMapper;

    // 특정 습득 게시글 댓글 목록 조회
    @Override
    public List<FoundCommentVO> getCommentsByNum(Long num) {
        return foundCommentMapper.selectCommentsByNum(num);
    }
    
    // 댓글 등록
    @Override
    public int insertComment(FoundCommentVO commentVO) {
        return foundCommentMapper.insertComment(commentVO);
    }


    // 댓글 단건 조회
    @Override
    public FoundCommentVO getCommentByCommentNum(Long commentNum) {
        return foundCommentMapper.selectCommentByCommentNum(commentNum);
    }

    // 댓글 수정
    @Override
    public int updateComment(FoundCommentVO commentVO) {
        return foundCommentMapper.updateComment(commentVO);
    }

    // 댓글 삭제
    @Override
    public int deleteComment(Long commentNum) {
        return foundCommentMapper.deleteComment(commentNum);
    } 
}
