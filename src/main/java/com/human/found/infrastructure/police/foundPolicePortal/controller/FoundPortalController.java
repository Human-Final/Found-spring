package com.human.found.infrastructure.police.foundPolicePortal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.human.found.domain.found.service.FoundPoliceService;
import com.human.found.infrastructure.police.foundPolicePortal.service.FoundPortalServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FoundPortalController {
    
    private final FoundPortalServiceImpl foundPortalService;

    @PostMapping("/admin/found-portal/import")
    public String importFoundPortalPost() throws Exception {
        foundPortalService.saveFoundPortalData();
        return "습득물 API 데이터 저장 완료";
    }

    @GetMapping("/found-portal/import-test")
    public String importFoundPortalGetTest() throws Exception {
        foundPortalService.saveFoundPortalData();
        return "습득물 API 데이터 저장 완료";
    }

    @RestController
    @RequiredArgsConstructor
    public class FoundPoliceTestController {

        private final FoundPoliceService foundPoliceService;

        @GetMapping("/test/found-police-refresh")
        public String refreshFoundPolice() {
            return foundPoliceService.savePoliceFoundItems();
        }
    }
}
