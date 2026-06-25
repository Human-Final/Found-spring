package com.human.found.domain.comment.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.LostCommentService;
import com.human.found.domain.comment.vo.CommentVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LostCommentController {

    private final LostCommentService lostCommentService;

    // 댓글 등록
    @PostMapping("/api/lost/{num}/comments")
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

        commentVO.setNum(num);
        commentVO.setId(principal.getName());
        commentVO.setDataSource(dataSource);

        lostCommentService.addComment(commentVO);

        return "redirect:/api/lost/detail/" + atcId;
    }

    // 댓글 수정
    @PostMapping("/api/lost/comments/{commentNum}/update")
    public String updateComment(
            @PathVariable("commentNum") Long commentNum,
            @RequestParam("atcId") String atcId,
            CommentVO commentVO,
            Principal principal,
            RedirectAttributes rttr
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        CommentVO savedComment =
                lostCommentService.getCommentByCommentNum(commentNum);

        if (savedComment == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 댓글입니다.");
            return "redirect:/api/lost";
        }

        if (!savedComment.getId().equals(principal.getName())) {
            rttr.addFlashAttribute("error", "댓글 수정 권한이 없습니다.");
            return "redirect:/api/lost/detail/" + atcId;
        }

        commentVO.setCommentNum(commentNum);
        commentVO.setId(principal.getName());

        lostCommentService.updateComment(commentVO);

        return "redirect:/api/lost/detail/" + atcId;
    }

    // 분실물 게시글의 댓글 삭제
    @PostMapping("/api/lost/comments/{commentNum}/delete")
    public String deleteComment(
            @PathVariable("commentNum") Long commentNum,
            @RequestParam("atcId") String atcId,
            Principal principal,
            RedirectAttributes rttr
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        CommentVO savedComment =
                lostCommentService.getCommentByCommentNum(commentNum);

        if (savedComment == null) {
            rttr.addFlashAttribute("error", "존재하지 않는 댓글입니다.");
            return "redirect:/api/lost";
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isOwner =
                savedComment.getId().equals(principal.getName());

        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(auth ->
                                auth.getAuthority().equals("ROLE_ADMIN")
                             || auth.getAuthority().equals("ROLE_MANAGER"));

        if (!isOwner && !isAdmin) {
            rttr.addFlashAttribute("error", "댓글 삭제 권한이 없습니다.");
            return "redirect:/api/lost/detail/" + atcId;
        }

        lostCommentService.deleteComment(commentNum);

        return "redirect:/api/lost/detail/" + atcId;
    }
}