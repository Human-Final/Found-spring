package com.human.found.domain.comment.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.FoundCommentService;
import com.human.found.domain.comment.vo.FoundCommentVO;
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
        FoundCommentVO commentVO,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if(principal == null) {
            return "redirect:/login";
        }

        FoundVO foundVO = foundService.getFoundByNum(num);

        if(foundVO == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 게시글입니다.");
            return "redirect:/api/found";
        }

        commentVO.setNum(num);
        commentVO.setId(principal.getName());

        foundCommentService.insertComment(commentVO);

        return "redirect:/api/found/detail/" + foundVO.getAtcId();
    }

    // 댓글 수정
    @PostMapping("/api/found/comments/{commentNum}/update")
    public String updateComment(
        @PathVariable("commentNum") Long commentNum,
        FoundCommentVO commentVO,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if(principal == null) {
            return "redirect:/login";
        }

        FoundCommentVO savedComment = foundCommentService.getCommentByCommentNum(commentNum);

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

    // 댓글 삭제
    @PostMapping("/api/found/comments/{commentNum}/delete")
    public String deleteComment(
        @PathVariable("commentNum") Long commentNum,
        Principal principal,
        RedirectAttributes rttr
    ) {
        if(principal == null) {
            return "redirect:/login";
        }

        FoundCommentVO savedComment = foundCommentService.getCommentByCommentNum(commentNum);

        if(savedComment == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 댓글입니다.");
            return "redirect:/api/found";
        }

        if (!savedComment.getId().equals(principal.getName())) {
            rttr.addFlashAttribute("error", "댓글 삭제 권한이 없습니다.");
            return "redirect:/api/found";
        }

        foundCommentService.deleteComment(commentNum);

        FoundVO foundVO = foundService.getFoundByNum(savedComment.getNum());
    
        return "redirect:/api/found/detail/" + foundVO.getAtcId();
    }
}
