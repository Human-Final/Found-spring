package com.human.found.domain.comment.controller;

import java.security.Principal; 
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.FoundCommentService;
import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.found.service.FoundService;
import com.human.found.domain.found.vo.FoundVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FoundCommentController {
    
    private final FoundCommentService foundCommentService;
    private final FoundService foundService;

    // 댓글 등록
    @PostMapping("/api/found/{num}/comments")
    public String insertComment(
        @PathVariable("num") Long num,
        @RequestParam("atcId") String atcId,
        @RequestParam("dataSource") String dataSource,
        CommentVO commentVO,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        // 유저가 작성한 게시글에 댓글 달렸다면 해당 유저에게 이메일 전송해주기
        if (atcId != null && atcId.startsWith("USER")) {
            String userEmail=foundCommentService.findUserEmailByAtcId(atcId);
            foundCommentService.emailNotify(userEmail, atcId);
            System.out.println(atcId);
            System.out.println(userEmail);
        }
        
        commentVO.setNum(num);
        commentVO.setId(principal.getName());
        commentVO.setDataSource(dataSource);

        foundCommentService.insertComment(commentVO);

        return "redirect:/api/found/detail/" + atcId;
    }

    // 댓글 수정
    @PostMapping("/api/found/comments/{commentNum}/update")
    public String updateComment(
        @PathVariable("commentNum") Long commentNum,
        CommentVO commentVO,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if(principal == null) {
            return "redirect:/login";
        }

        CommentVO savedComment = foundCommentService.getCommentByCommentNum(commentNum);

        if(savedComment == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 댓글입니다.");
            return "redirect:/api/found";
        }

        if(!savedComment.getId().equals(principal.getName())) {
            rttr.addFlashAttribute("error", "댓글 수정 권한이 없습니다.");
            return "redirect:/api/found";
        }

        commentVO.setCommentNum(commentNum);
        foundCommentService.updateComment(commentVO);

        FoundVO foundVO = foundService.getFoundByNum(savedComment.getNum());

        return "redirect:/api/found/detail/" + foundVO.getAtcId();
    }

    // 습득물 게시글의 댓글 삭제
    @PostMapping("/api/found/comments/{commentNum}/delete")
    public String deleteComment(
        @PathVariable("commentNum") Long commentNum,
        @RequestParam("atcId") String atcId,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        CommentVO savedComment = foundCommentService.getCommentByCommentNum(commentNum);

        if (savedComment == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 댓글입니다.");
            return "redirect:/api/found";
        }
        
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isOwner = savedComment.getId().equals(principal.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth ->
                        auth.getAuthority().equals("ROLE_ADMIN")
                    || auth.getAuthority().equals("ROLE_MANAGER"));

        if (!isOwner && !isAdmin) {
            rttr.addFlashAttribute("error", "댓글 삭제 권한이 없습니다.");
            return "redirect:/api/found/detail/" + atcId;
        }

        foundCommentService.deleteComment(commentNum);

        return "redirect:/api/found/detail/" + atcId;
    }
}
