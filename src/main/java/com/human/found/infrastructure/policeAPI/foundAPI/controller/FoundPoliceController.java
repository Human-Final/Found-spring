package com.human.found.infrastructure.policeAPI.foundAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.human.found.infrastructure.policeAPI.foundAPI.service.FoundPoliceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FoundPoliceController {
    
    // 경찰청 습득물 서비스 객체
    private final FoundPoliceService policeFoundService;

    /**
     * 경찰청 습득물 조회 요청
     * URL : http://localhost:8080/police/found/save
     */
    @GetMapping("/police/found/save")
    @ResponseBody
    public String savePoliceFounditems() {
        return policeFoundService.savePoliceFoundItems();
    }
}
