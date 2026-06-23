package com.human.found.domain.lost.controller;

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

import com.human.found.domain.comment.service.LostCommentService;
import com.human.found.domain.lost.service.LostService;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.infrastructure.map.KakaoMapConfig;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;





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
        
        // [입구 컷] 검증 에러가 있다면 정상 로직 수행 전에 리턴!    
        System.out.println("컨트롤러 진입");
        if(bindingResult.hasErrors()){
            
            bindingResult.getFieldErrors()
            .forEach(error -> System.out.println(
                error.getField() + " = " + error.getDefaultMessage()));
            

            model.addAttribute("writetype", "lost");
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
    //조회
    @GetMapping("/api/lost")
    public String Lostlist(Model model) {
        List<LostVO>getList=lostService.getLostlist();
        model.addAttribute("getList", getList);
        return "lost/list";
    }

    @GetMapping("/write")
    public String lostWriteForm(Model model) {
        // 화면에 습득물(lost) 타입을 구분하기 위한 값 전달
        model.addAttribute("lostVO",new LostVO());
        model.addAttribute("writetype", "lost");
        return "found/write";
    }
    //삭제
    @DeleteMapping("/api/lost/{atcId}")
    //비밀번호/게시글번호(lostnum)/사용자검증(principla)/
    // 삭제가 성공하거나 실패했을 때, 리다이렉트 되는 페이지(목록이나 상세페이지)로 일회성 메시지 전달
    public String DeletedLost(
        @RequestParam("password")String inputpw,@PathVariable("atcId") String atcId,Authentication authentication,
        RedirectAttributes redirectAttributes){
        
        //로그인체크
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/api/lost/";
        }
        String loginid=authentication.getName();
        //로그인한 유저의 권한 목록 중 관리자 권한 (ADMIN) 이 있는지 확인
        boolean isAdmin=authentication.getAuthorities().stream()
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||auth.getAuthority().equals("ADMIN"));

        try {
            //서비스단에 글 id 입력번호, 로그인한 유저 id 를 넘겨받아 검증 및 삭제
            lostService.deletelost(inputpw,atcId,loginid,isAdmin);
            redirectAttributes.addFlashAttribute("Message", "삭제완료");
            return "redirect:/api/lost/";
        } catch (Exception e) {
            // 검증 실패 시 에러 메시지를 들고 원래 상세 페이지로 리턴
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());           
                return "redirect:/api/lost/detail/"+atcId;           
        }        
    }
    //==상세보기==
    @GetMapping("/api/lost/detail/{atcId}")
    public String lostDetaile(@PathVariable("atcId")String atcId,Model model) {
        LostVO lostVO=lostService.lostdetail(atcId);
        model.addAttribute("lostVO", lostVO);
        model.addAttribute("comments", lostCommentService.getComments(
            lostVO.getNum(), lostVO.getDataSource()
        )
    );
        model.addAttribute("kakaoMapJsKey", kakaoMapConfig.getJsKey());
        return "lost/detail";
    }
    
        
}
    
    
    

