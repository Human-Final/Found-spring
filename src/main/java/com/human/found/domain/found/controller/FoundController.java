package com.human.found.domain.found.controller;

import java.security.Principal;
import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.FoundCommentService;
import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.found.service.FoundService;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.global.common.paging.PagingVO;
import com.human.found.infrastructure.map.KakaoMapConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import jakarta.servlet.http.Cookie;



@Controller
@AllArgsConstructor
public class FoundController {
    private final FoundService foundService;
    private final KakaoMapConfig kakaoMapConfig;
    private final FoundCommentService foundCommentService;
    

    //=====습득물 등록====
    //BindingResult는 Spring MVC에서 폼 데이터 바인딩 결과와 검증(Validation) 결과를 저장하는 객체
    @PostMapping("/api/found")
    public String postRegister(@Valid @ModelAttribute("foundVO") FoundVO foundVO, 
        BindingResult bindingResult, Model model,
        @RequestParam(value="files",required = false) MultipartFile[]files, Principal principal ) {
        
        if(bindingResult.hasErrors()){
            model.addAttribute("boardType", "found");
            return "found/write";
        }    
        //작성자 자동설정
        if (principal != null){
            foundVO.setId(principal.getName());
        }else{
            foundVO.setId("test_member");
        }

        foundVO.setAtcId("USER-" + System.currentTimeMillis());

        foundService.Register(foundVO,files);
        return "redirect:/api/found";
    }
    //=======조회======   
    @GetMapping("/api/found")
    public String getFoundList(
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String colorSelect,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String author,  // 습득자명
            @RequestParam(required = false) String status,  // 보관 상태
            @RequestParam(required = false) String keyword, // 물품명
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        // 1. 페이징 음수 오프셋 방지선
        if (page < 1) {
            page = 1;
        }

        // 2. 페이징 VO 인프라 세팅 (보여주신 기본 스펙 유지)
        PagingVO pagingVO = new PagingVO();
        pagingVO.setPage(page);
        pagingVO.setSize(10);
        pagingVO.setPageBlock(10);
        
        // 3. 습득물 조건에 맞는 전체 데이터 개수 카운트 실행
        int totalCount = foundService.getTotalSearchCount(category, subCategory, colorSelect, startDate, endDate, author, status, keyword);
        pagingVO.setTotalCount(totalCount); 
        
        // 4. [놓치면 안 되는 핵심] 페이징 바 번호 계산 연산 가동!
        pagingVO.pageInfo(totalCount); 
  
        // 5. 최종 조건별 페이징 리스트 조회
        List<FoundVO> getList = foundService.searchFoundItems(category, subCategory, colorSelect, startDate, endDate, author, status, keyword, sort, pagingVO);

        model.addAttribute("getList", getList);
        
        // 6. 화면단 옵션 상태 및 텍스트 유지 백 바인딩
        model.addAttribute("selectedCategories", category);
        model.addAttribute("selectedSubCategory", subCategory);
        model.addAttribute("colorSelected", colorSelect);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedAuthor", author);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", pagingVO); 
        model.addAttribute("isEdit", false);
        
        return "found/list";
    }
    
    @GetMapping("/api/write")
    public String writeForm(@RequestParam(defaultValue = "found") String boardType,
                                Model model) {
        // 화면에 습득물(found) 타입을 구분하기 위한 값 전달
        // model.addAttribute("foundVO", new FoundVO());
        
        // 기본값 설정
        model.addAttribute("boardType", boardType);
        model.addAttribute("isEdit", false);

        if("lost".equals(boardType)){
            model.addAttribute("lostVO", new LostVO());
        }else {
            model.addAttribute("foundVO", new FoundVO());
        }
        return "found/write";
    }
    
    //========삭제========
    @DeleteMapping("/api/found/{atcId}")
    public String deletefound(
        @RequestParam(value = "password", required = false)String inputpw,
        @PathVariable("atcId")String atcId,
        Authentication authentication ,RedirectAttributes redirectAttributes) {
        
            
        //로그인체크
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/api/found";
        }
        String loginid =authentication.getName(); 
        //로그인한 유저의 권한 목록 중 관리자 권한 (ADMIN) 이 있는지 확인
        boolean isAdmin=authentication.getAuthorities().stream()
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ADMIN")
                        || auth.getAuthority().equals("ROLE_MANAGER")
                        || auth.getAuthority().equals("MANAGER"));

        try {
            //서비스단에 글 id 입력번호, 로그인한 유저 id 를 넘겨받아 검증 및 삭제
            foundService.deletefound(atcId,inputpw,loginid,isAdmin);
            redirectAttributes.addFlashAttribute("Message", "삭제완료");
            
            return "redirect:/api/found";

        } catch (Exception e) {
            // 검증 실패 시 에러 메시지를 들고 원래 상세 페이지로 리턴
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());

                return "redirect:/api/found/detail/"+atcId;           
        }                
    }
    
    //====상세보기=====
    @GetMapping("/api/found/detail/{atcId}")
    public String foundgetdetail(
        @PathVariable("atcId") String atcId, Model model,
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
                    foundService.viewCountPlus(atcId);
                    
                    // 기존 값에 누적하지 않고, 현재 atcId 한 개만 깔끔하게 덮어씁니다.
                    viewCookie.setValue("[" + atcId + "]"); 
                    viewCookie.setPath("/");
                    viewCookie.setMaxAge(60 * 60 * 1); // 1시간 유지
                    response.addCookie(viewCookie);
                }
            } else {
                // 'lostViewLogs' 쿠키가 아예 존재하지 않는 최초의 클라이언트인 경우
                foundService.viewCountPlus(atcId); // 조회수 증가 호출
                
                // 새로운 쿠키를 생성하여 현재 글 id를 담습니다.
                Cookie newCookie = new Cookie("lostViewLogs", "[" + atcId + "]");
                newCookie.setPath("/");
                newCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
                response.addCookie(newCookie);     // 브라우저로 최초 쿠키 발급
            }

            FoundVO foundVO = foundService.foundgetdetail(atcId);

            List<CommentVO> commentList =
                foundCommentService.getCommentsByNum(
                        foundVO.getNum(),
                        foundVO.getDataSource()
                );

            model.addAttribute("foundVO", foundVO);
            model.addAttribute("commentList", commentList);
            model.addAttribute("kakaoMapJsKey", kakaoMapConfig.getJsKey());

            return "found/detail";
    }
    
    //======수정화면==
    @GetMapping("/api/found/detail/{atcId}/edit")
    public String FoundEditForm(@PathVariable("atcId")String atcId,Model model,Authentication authentication,
    RedirectAttributes redirectAttributes) {
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다");
            return "redirect:/api/found";
        }
        FoundVO foundVO = foundService.foundgetdetail(atcId);

        //작성자 본인 확인 방어코드
        String loginid=authentication.getName();
        boolean isAdmin=authentication.getAuthorities().stream()
            .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ADMIN")
                        || auth.getAuthority().equals("ROLE_MANAGER")
                        || auth.getAuthority().equals("MANAGER"));
        if(!isAdmin && (foundVO.getId()==null||!foundVO.getId().equals(loginid))){
            redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 글만 수정할 수 있습니다");
            return "redirect:/api/found/detail/"+atcId;    
        }    
        model.addAttribute("foundVO", foundVO);
        model.addAttribute("boardType", "found");
        model.addAttribute("isEdit", true);
        
        return "found/write";
    }
    
    // 실제 데이터 수정처리
    @PostMapping("/api/found/update")
    public String FoundUpdate(@Valid @ModelAttribute("foundVO")FoundVO foundVO,
    BindingResult bindingResult,Model model,
    @RequestParam(value = "files",required = false) MultipartFile files[],
    @RequestParam(value = "deleteFiles",required = false )List<String>deleteFiles) {
    
    //[입력값 유지 및 임시저장 기능] 검증 에러 발생 시 작성하던 내용 그대로 다시 폼으로 백! 
    if(bindingResult.hasErrors()){
        model.addAttribute("boardType", "found");
        model.addAttribute("isEdit", true);
        // 이미 매핑된 foundVO가 model에 담겨 있으므로 입력했던 값들이 폼에 그대로 유지
        return "found/write";
    }   
        foundService.UpdateFound(foundVO,files,deleteFiles);
        return "redirect:/api/found/detail/"+foundVO.getAtcId();
    }

    
    
}
