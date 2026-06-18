package com.human.found.domain.lost.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.service.LostService;
import com.human.found.domain.lost.vo.LostVO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
@AllArgsConstructor
public class LostController {
    private final LostService lostService;
    @PostMapping("/api/lost")
    public String LostRegister(@Valid @ModelAttribute LostVO lostVO,BindingResult bindingResult,Model model,
        @RequestParam(value="files",required = false) MultipartFile[]files, Principal principal) {
        
        // [입구 컷] 검증 에러가 있다면 정상 로직 수행 전에 리턴!    
        if(bindingResult.hasErrors()){
            model.addAttribute("writetype", "lost");
            return "write";
        }
            
        if(principal != null){
            lostVO.setId(principal.getName());
        }else{
            lostVO.setId("test_member");
        }

        lostService.LostRegister(lostVO,files);
        
        return "redirect:/api/lost";
    }
    
    @GetMapping("/api/lost")
    public String Lostlist(Model model) {
        List<LostVO>getList=lostService.getLostlist();
        model.addAttribute("getList", getList);
        return "lost/list";
    }

    // @GetMapping("/api/write")
    // public String lostWriteForm(Model model) {
    //     // 화면에 습득물(lost) 타입을 구분하기 위한 값 전달
    //     model.addAttribute("lostVO",new LostVO());
    //     model.addAttribute("writetype", "lost");
    //     return "found/write";
    // }
    
    
    
    
}
