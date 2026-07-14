package com.human.found.domain.comment.service;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.comment.mapper.LostCommentMapper;
import com.human.found.domain.comment.vo.CommentVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LostCommentServiceImpl implements LostCommentService{

    private final LostCommentMapper lostCommentMapper;
    private final JavaMailSender mailSender;

    // 댓글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CommentVO> getComments(Long num, String dataSource) {
        return lostCommentMapper.findComments(num, dataSource);
    }

    // 댓글 등록
    @Override
    public void addComment(CommentVO comment) {
        lostCommentMapper.insertComment(comment);
    }

    // 댓글 단건 조회
    @Override
    public CommentVO getCommentByCommentNum(Long commentNum) {
        return lostCommentMapper.findByCommentNum(commentNum);
    }

    // 댓글 수정
    @Override
    public void updateComment(CommentVO comment) {
        lostCommentMapper.updateComment(comment);
    }

    // 댓글 1개 삭제
    @Override
    public void deleteComment(Long commentNum) {
        lostCommentMapper.deleteComment(commentNum);
    }
    
    // 게시글 ID로 유저의 이메일 찾기
    @Override
    public String findUserEmailByAtcId(String atcId) {
        return lostCommentMapper.findUserEmailByAtcId(atcId);
    }

    @Async
    @Override
    public void emailNotify(String userEmail, String atcId) {
        try {
            if (userEmail == null || userEmail.isBlank()) {
                return;
            }    

            SimpleMailMessage message = new SimpleMailMessage();
                
            message.setTo(userEmail); // 수신자: 게시글 작성자의 이메일
            message.setSubject("[분실물센터] 내 분실물 게시글에 새로운 댓글이 등록되었습니다.");
            message.setText("안녕하세요. FOUND AI 기반 분실물 찾기 서비스입니다.\n\n"
                    + "회원님이 작성하신 분실물 게시글에 새로운 댓글이 등록되었습니다.\n"
                    + "아래 링크를 통해 확인해 주시기 바랍니다.\n"
                    + "👉 바로가기: http://localhost:8080/api/found/detail/" + atcId);
                
            mailSender.send(message); // 🚀 실제 메일 발송!
            // System.out.println("[메일 알림] 댓글 작성 알림 이메일 발송 성공 -> " + userEmail);
            
            return;
            
        } catch (Exception e) {
            // 💡 메일 발송에 실패해도 로그만 찍고 넘어가므로, 댓글 등록 프로세스는 안전하게 유지됩니다.
            // System.err.println("[메일 알림 에러] 이메일 발송 중 문제가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            System.err.println("[메일 알림/에러] 이메일 발송 중 문제가 발생했습니다: " + e.getMessage());
            return;
        }
    }
}
