package com.human.found.domain.lost.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.lost.service.LostPoliceServiceImpl;
import com.human.found.domain.lost.vo.LostVO;

@Controller
@RequestMapping("/lost")
public class LostPoliceController {

    @Autowired
    private LostPoliceServiceImpl lostService;

    @Autowired
    private com.human.found.infrastructure.map.KakaoMapConfig kakaoMapConfig;

    // 1. 목록 조회 화면
    @GetMapping("/list")
    public String list(Model model) {
        List<LostVO> goodsList = lostService.getLostGoodsFromDB();
        model.addAttribute("goodsList", goodsList);
        return "lost/list";
    }

    // 3. 상세페이지 (매핑 경로를 /lost/detail로 고쳤습니다)
    @GetMapping("/detail")
    public String detail(@RequestParam("atcId") String atcId, Model model) {
        LostVO goods = lostService.getDetailByAtcId(atcId);
        model.addAttribute("goods", goods);
        model.addAttribute("kakaoJsKey", kakaoMapConfig.getJsKey()); // 키값 전달
        return "lost/detail"; // 경로 확인하십시오
    }
}