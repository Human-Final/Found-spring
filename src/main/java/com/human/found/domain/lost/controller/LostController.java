package com.human.found.domain.lost.controller;

import java.security.Principal;
import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.service.LostService;
import com.human.found.domain.lost.vo.LostVO;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@Tag(name = "분실물 관리 테스트 API", description = "VS Code에서 확인하는 스웨거")
@RestController
@AllArgsConstructor
public class LostController {
    private final LostService lostService;
    @PostMapping("/api/lost")
    public String LostRegister(@ModelAttribute LostVO lostVO,MultipartFile[]files, Principal principal) {
        
        if(principal != null){
            lostVO.setId(principal.getName());
        }else{
            lostVO.setId("test_member");
        }
        
        lostService.LostRegister(lostVO,files);
        
        return "성공";
    }
    
    @GetMapping("/api/lost")
    public List<LostVO> Lostlist() {
        List<LostVO>lostlist=lostService.getLostlist();
        return lostlist;
    }
    
    
}
