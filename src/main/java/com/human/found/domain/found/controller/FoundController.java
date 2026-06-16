package com.human.found.domain.found.controller;

import org.springframework.http.MediaType;//스웨거 확인
import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.service.FoundService;
import com.human.found.domain.found.vo.FoundVO;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Tag(name = "습득물 관리 테스트 API", description = "VS Code에서 확인하는 스웨거")
@RestController
@AllArgsConstructor
public class FoundController {
    private final FoundService foundService;

    //습득물 등록
    @PostMapping(value = "/api/found", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String postRegister(@ModelAttribute FoundVO foundvo ,
        
        @RequestParam(value="files",required = false) MultipartFile[]files, Principal principal ) {

        //작성자 자동설정
        if (principal != null){
            foundvo.setId(principal.getName());
        }else{
            foundvo.setId("test_member");
        }

        foundService.Register(foundvo,files);

        return "성공";
    }
    
    @GetMapping("/api/found")
    public List<FoundVO> getFoundList() {
        List<FoundVO>getList = foundService.getFoundList();
        return getList;
    }
    
}
