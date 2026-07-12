package com.human.found.domain.lost.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.LostCommentService;
import com.human.found.domain.lost.service.LostService;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.global.common.paging.PagingVO;
import com.human.found.infrastructure.map.KakaoMapConfig;
import com.human.found.infrastructure.police.lostAPI.service.LostPoliceService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Controller
@AllArgsConstructor
public class LostController {
    private final LostService lostService;
    private final LostCommentService lostCommentService;
    private final KakaoMapConfig kakaoMapConfig;


    @PostMapping("/api/lost")
    public String LostRegister(@Valid @ModelAttribute LostVO lostVO,
        BindingResult bindingResult,Model model,
        @RequestParam(value="files",required = false) MultipartFile[]files, Principal principal) {
            
        if(bindingResult.hasErrors()){
            
            bindingResult.getFieldErrors()
            .forEach(error -> System.out.println(
                error.getField() + " = " + error.getDefaultMessage()));
            
            model.addAttribute("boardType", "lost");
            return "found/write";
        }
            
        if(principal != null){
            lostVO.setId(principal.getName());
        }else{
            lostVO.setId("test_member");
        }

        lostVO.setAtcId("USER-" + System.currentTimeMillis());

        System.out.println("서비스 호출 직전");
        lostService.LostRegister(lostVO,files);
        
        return "redirect:/api/lost";
    }
    
    // 카테고리를 활용하는 조회(전체조회 포함)
    @GetMapping("/api/lost")
    public String Lostlist(
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword, // 습득물명
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        if (page < 1) {
            page = 1;
        }

        // 1. 기존 페이징 VO 인프라 세팅 (보여주신 스펙 100% 유지)
        PagingVO pagingVO = new PagingVO();
        pagingVO.setPage(page);
        pagingVO.setSize(10);
        pagingVO.setPageBlock(10);
        
        // 2. 하단 페이징 바(1 2 3) 계산을 위해 검색 조건에 맞는 '총 데이터 개수'를 먼저 구해옵니다.
        int totalCount = lostService.getTotalSearchCount(category, subCategory, startDate, endDate, author, status, keyword);
        pagingVO.setTotalCount(totalCount); // VO 내부에 총 개수를 심어 endPage, prev, next 블록 자동 계산 작동
        pagingVO.pageInfo(totalCount);
  
        // 3. 기존 6대 필터 인자 뒤에, 페이징 처리를 위해 pagingVO를 마지막 인자로 주입하여 서비스를 호출합니다!
        List<LostVO> getList = lostService.searchLostItems(category, subCategory, startDate, endDate, author, status, keyword, sort, pagingVO);
        
        model.addAttribute("getList", getList);
        
        // 검색 필터 및 스타일 복원용 모델 바인딩 (순정 상태 유지)
        model.addAttribute("selectedCategories", category);
        model.addAttribute("selectedSubCategory", subCategory);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedAuthor", author);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", pagingVO);
        model.addAttribute("isEdit", false);
        
        return "lost/list";
    }



    @GetMapping("/write")
    public String lostWriteForm(Model model) {
        // 화면에 습득물(lost) 타입을 구분하기 위한 값 전달
        model.addAttribute("lostVO",new LostVO());
        model.addAttribute("boardType", "lost");
        return "found/write";
    }
    //삭제
    @DeleteMapping("/api/lost/{atcId}")
    //비밀번호/게시글번호(lostnum)/사용자검증(principla)/
    // 삭제가 성공하거나 실패했을 때, 리다이렉트 되는 페이지(목록이나 상세페이지)로 일회성 메시지 전달
    public String DeletedLost(
        @RequestParam("password")String inputpw,@PathVariable("atcId") String atcId,
        Authentication authentication,
        RedirectAttributes redirectAttributes){
        
        //로그인체크
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/api/lost";
        }
        String loginid=authentication.getName();
        //로그인한 유저의 권한 목록 중 관리자 권한 (ADMIN) 이 있는지 확인
        boolean isAdmin=authentication.getAuthorities().stream()
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ADMIN")
                        || auth.getAuthority().equals("ROLE_MANAGER")
                        || auth.getAuthority().equals("MANAGER"));

        try {
            //서비스단에 글 id 입력번호, 로그인한 유저 id 를 넘겨받아 검증 및 삭제
            lostService.deletelost(inputpw,atcId,loginid,isAdmin);
            redirectAttributes.addFlashAttribute("Message", "삭제완료");
            return "redirect:/api/lost";
        } catch (Exception e) {
            // 검증 실패 시 에러 메시지를 들고 원래 상세 페이지로 리턴
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());           
                return "redirect:/api/lost/detail/"+atcId;           
        }        
    }
    //==상세보기==
    @GetMapping("/api/lost/detail/{atcId}")
    public String lostDetaile(
        @PathVariable("atcId")String atcId,Model model,
        HttpServletRequest request, HttpServletResponse response) {

            Cookie[] cookies = request.getCookies();
            Cookie viewCookie = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lostViewLogs".equals(cookie.getName())) {
                        viewCookie = cookie;
                        break;
                    }
                }
            }
            
            if (viewCookie != null) {
                if (!viewCookie.getValue().contains("[" + atcId + "]")) {
                    lostService.viewCountPlus(atcId);
                    
                    // 기존 값에 누적하지 않고, 현재 atcId 한 개만 깔끔하게 덮어씁니다.
                    viewCookie.setValue("[" + atcId + "]"); 
                    viewCookie.setPath("/");
                    viewCookie.setMaxAge(60 * 60 * 1); // 1시간 유지
                    response.addCookie(viewCookie);
                }
            } else {
                // 'lostViewLogs' 쿠키가 아예 존재하지 않는 최초의 클라이언트인 경우
                lostService.viewCountPlus(atcId); // 조회수 증가 호출
                
                // 새로운 쿠키를 생성하여 현재 글 id를 담습니다.
                Cookie newCookie = new Cookie("lostViewLogs", "[" + atcId + "]");
                newCookie.setPath("/");
                newCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
                response.addCookie(newCookie);     // 브라우저로 최초 쿠키 발급
            }
            
            LostVO lostVO=lostService.lostdetail(atcId);

            if(lostVO == null){
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다.");
            }


            model.addAttribute("lostVO", lostVO);
            model.addAttribute("comments", lostCommentService.getComments(
                lostVO.getNum(), lostVO.getDataSource())
            );
            model.addAttribute("kakaoMapJsKey", kakaoMapConfig.getJsKey());
            return "lost/detail";
    }
    
    //수정
    @GetMapping("/api/lost/detail/{atcId}/edit")
    public String LostEditForm(@PathVariable("atcId")String atcId,Model model,Authentication authentication,
    RedirectAttributes redirectAttributes) {
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errormessage", "로그인이 필요합니다");
            return "redirect:/api/lost";
        }
        LostVO lostVO=lostService.lostdetail(atcId);
        // 작성자 본인 확인 방어 코드
        String loginId=authentication.getName();
        boolean isAdmin=authentication.getAuthorities().stream()
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ADMIN")
                        || auth.getAuthority().equals("ROLE_MANAGER")
                        || auth.getAuthority().equals("MANAGER"));
        if(!isAdmin &&(lostVO.getId()==null||!lostVO.getId().equals(loginId))){
            redirectAttributes.addFlashAttribute("errormessage", "본인이 작성한 게시글만 수정할 수 있습니다");
            return "redirect:/api/lost/detail/"+atcId;
        }
        model.addAttribute("lostVO", lostVO);
        model.addAttribute("boardType", "lost");
        model.addAttribute("isEdit", true);
        return "found/write";
    }
    //실제 데이터 수정
    @PostMapping("api/lost/update")
    public String LostUpdate(@Valid @ModelAttribute("lostVO")LostVO lostVO,
            BindingResult bindingResult,Model model,
            @RequestParam(value = "files",required = false) MultipartFile[]files,
            @RequestParam(value = "deleteFiles",required = false)List<String> deleteFiles) {

        //[입력값 유지 및 임시저장 기능] 검증 에러 발생 시 작성하던 내용 그대로 다시 폼으로 백!
        if(bindingResult.hasErrors()){
            model.addAttribute("boardType", "lost");
            model.addAttribute("isEdit", true);
            return "found/write";
        }
        lostService.UpdateLost(lostVO,files,deleteFiles);
        return "redirect:/api/lost/detail/"+lostVO.getAtcId();
    }
}