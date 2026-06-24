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
import com.human.found.global.common.paging.PagingVO;
import com.human.found.infrastructure.map.KakaoMapConfig;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;



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
            model.addAttribute("writetype", "found");
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
        Model model, 
        @RequestParam(name="page", defaultValue = "1") int page) {

        PagingVO pagingVO = new PagingVO();
        pagingVO.setPage(page);
        pagingVO.setSize(10);
        pagingVO.setPageBlock((10));

        List<FoundVO>getList = foundService.getFoundList(pagingVO);
        model.addAttribute("paging", pagingVO);
        model.addAttribute("getList", getList);
        // model.addAttribute("countTotalFound", countTotalFound);

        return "found/list";
    }
    
    @GetMapping("/api/write")
    public String foundWriteForm(Model model) {
        // 화면에 습득물(found) 타입을 구분하기 위한 값 전달
        model.addAttribute("foundVO", new FoundVO());
        
        // 기본값 설정
        model.addAttribute("writetype", "found");
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
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||auth.getAuthority().equals("ADMIN"));
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
    public String foundgetdetail(@PathVariable("atcId") String atcId, Model model) {

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
    public String FoundEditForm(@PathVariable("atcId")String atcId,Model model,Authentication authentication,RedirectAttributes redirectAttributes) {
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다");
            return "redirect:/api/found";
        }
        FoundVO foundVO = foundService.foundgetdetail(atcId);

        //작성자 본인 확인 방어코드
        String loginid=authentication.getName();
        boolean isAdmin=authentication.getAuthorities().stream()
            .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||
            auth.getAuthority().equals("ADMIN"));
        if(!isAdmin && (foundVO.getId()==null||!foundVO.getId().equals(loginid))){
            redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 글만 수정할 수 있습니다");
            return "redirect:/api/found/detail/"+atcId;    
        }    
        model.addAttribute("foundVO", foundVO);
        model.addAttribute("writetype", "found");
        model.addAttribute("isEdit", true);
        
        return "found/write";
    }
    



    // 실제 데이터 수정처리
    @PostMapping("/api/found/update")
    public String FoundUpdate(@Valid @ModelAttribute("foundVO")FoundVO foundVO,
    BindingResult bindingResult,Model model,@RequestParam(value = "files",required = false)MultipartFile files[],
    @RequestParam(value = "deleteFiles",required = false )List<String>deleteFiles) {
    
    //[입력값 유지 및 임시저장 기능] 검증 에러 발생 시 작성하던 내용 그대로 다시 폼으로 백! 
    if(bindingResult.hasErrors()){
        model.addAttribute("writetype", "found");
        model.addAttribute("isEdit", true);
        // 이미 매핑된 foundVO가 model에 담겨 있으므로 입력했던 값들이 폼에 그대로 유지
        return "found/write";
    }   
        foundService.UpdateFound(foundVO,files,deleteFiles);
        return "redirect:/api/found/detail/"+foundVO.getAtcId();
    }

    
    
}
