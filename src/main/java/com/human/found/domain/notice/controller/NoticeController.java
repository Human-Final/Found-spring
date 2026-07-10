package com.human.found.domain.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.notice.service.NoticeService;
import com.human.found.domain.notice.vo.NoticeVO;
import com.human.found.global.common.paging.PagingVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 1. 공지사항 전체 목록 (방문자, 로그인 유저 누구나 가능)
    @GetMapping("/list")
    public String list(PagingVO pagingVO, Model model) {
        model.addAttribute("list", noticeService.getNoticeList(pagingVO));
        model.addAttribute("paging", pagingVO);
        return "notice/list";
    }

    // 2. 공지사항 상세보기 (방문자, 로그인 유저 누구나 가능)
    @GetMapping("/detail")
    public String detail(@RequestParam("num") Long num, Model model) {
        model.addAttribute("notice", noticeService.getNoticeDetail(num));
        return "notice/detail";
    }

    // 3. 작성 페이지 이동 (관리자 전용)
    @GetMapping("/write")
    public String writeForm(HttpSession session, Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("notice", new NoticeVO());
        return "notice/write";
    }

    // 4. 작성 처리 (관리자 전용)
    @PostMapping("/write")
    public String write(NoticeVO notice, 
            java.security.Principal principal,
            RedirectAttributes rttr) {
        
        // 시큐리티 로그인 금고에서 현재 로그인한 유저의 ID를 직접 꺼내 세팅합니다 (null 방지)
        if (principal != null) {
            notice.setId(principal.getName()); 
        }
        
        if (notice.getIsPlanned() == null) notice.setIsPlanned(0);
        if (notice.getIsImportant() == null) notice.setIsImportant(0);

        try {
            noticeService.registerNotice(notice);
            rttr.addFlashAttribute("message", "공지사항이 등록되었습니다.");
            return "redirect:/api/notices/list";

        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/api/notices/write";

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "공지사항 등록 중 문제가 발생했습니다.");
            return "redirect:/api/notices/write";
        }

    }

    // 5. 수정할 기존 공지사항 갖고오기 (MANAGER, ADMIN 전용)
    // 기존에 등록된 데이터를 폼에 띄워줘야 하므로 서비스에서 기존 공지 내용을 조회해옵니다.
    @GetMapping("/edit")
    public String editForm(@RequestParam("num") Long num, Model model, HttpSession session) {
        model.addAttribute("isEdit", true); 
        // 수정할 타겟 데이터를 조회해서 모델에 적재 (조회수 증가 없는 별도 메서드나 상세 조회 활용)
        model.addAttribute("notice", noticeService.getNoticeForEditandDelete(num));
        return "notice/write"; 
    }

    // 해당 공지사항 수정해서 POST로 보내기 매핑
    @PostMapping("/edit")
    public String edit(NoticeVO notice, RedirectAttributes rttr) {

        // 작성 양식에서 체크박스를 해제하고 전송하면 null이 넘어오므로 0으로 확실히 보정
        if (notice.getIsPlanned() == null) notice.setIsPlanned(0);
        if (notice.getIsImportant() == null) notice.setIsImportant(0);

        try {
            // 서비스단을 통해 파일 업데이트 및 DB UPDATE 쿼리(updated_at 반영) 수행
            noticeService.modifyNotice(notice);

            rttr.addFlashAttribute("message", "공지사항이 수정되었습니다.");
            return "redirect:/api/notices/detail?num=" + notice.getNum();
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/api/notices/edit?num=" + notice.getNum();
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "공지사항 수정 중 문제가 발생했습니다.");
            return "redirect:/api/notices/edit?num=" + notice.getNum();
        }
    }
        
    // 5. 삭제 처리 (관리자 전용)
    @GetMapping("/delete")
    public String delete(@RequestParam("num") Long num, RedirectAttributes rttr) {
        try {
            NoticeVO notice = noticeService.getNoticeForEditandDelete(num);

            if (notice == null) {
                rttr.addFlashAttribute("errorMessage", "존재하지 않는 공지사항입니다.");
                return "redirect:/api/notices/list";
            }

            String imagePath = notice.getImagePath();

            noticeService.removeNotice(num, imagePath);

            rttr.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
            return "redirect:/api/notices/list";

        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/api/notices/list";

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "공지사항 삭제 중 문제가 발생했습니다.");
            return "redirect:/api/notices/list";
        }
    }
}
