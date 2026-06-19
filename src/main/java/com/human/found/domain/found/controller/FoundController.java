package com.human.found.domain.found.controller;

import java.security.Principal;
import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.found.service.FoundService;
import com.human.found.domain.found.vo.FoundVO;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;






@Controller
@AllArgsConstructor
public class FoundController {
    private final FoundService foundService;
    

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
    public String getFoundList(Model model) {
        List<FoundVO>getList = foundService.getFoundList();
        model.addAttribute("getList", getList);

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
    @PostMapping("/api/found/{foundNum}")
    public String deletefound(
        @RequestParam("password")String inputpw,@PathVariable("foundNum")Long foundNum,
        Principal principal ,RedirectAttributes redirectAttributes) {

            //로그인체크
        if(principal==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/api/found";
        } 
        //트라이 블록 밖에서 주소를 조립하기 위해 변수 선언
        String targetAtcId=null;
        try {
            //서비스단에 글 id 입력번호, 로그인한 유저 id 를 넘겨받아 검증 및 삭제
            targetAtcId=foundService.deletefound(foundNum,inputpw,principal.getName());
            redirectAttributes.addFlashAttribute("Message", "삭제완료");
            
            return "redirect:/api/found";

        } catch (Exception e) {
            // 검증 실패 시 에러 메시지를 들고 원래 상세 페이지로 리턴
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());

            if(targetAtcId!=null){
                return "redirect:/api/found/detail/"+targetAtcId;
            }else{
                return "redirect:/api/found";
            }            
        }                
    }
    
    //====상세보기=====
    @GetMapping("/api/found/detail/{atcId}")
    public String foundgetdetail(@PathVariable("atcId")String atcId,Model model) {
        FoundVO foundVO=foundService.foundgetdetail(atcId);
        model.addAttribute("foundVO",foundVO);
        return "found/detail";
    }
    
    
    
}
