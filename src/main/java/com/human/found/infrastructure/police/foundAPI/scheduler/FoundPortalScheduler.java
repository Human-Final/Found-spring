package com.human.found.infrastructure.police.foundAPI.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.human.found.infrastructure.police.foundAPI.service.FoundPortalService;
import com.human.found.infrastructure.police.foundAPI.service.FoundPortalServiceImpl;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FoundPortalScheduler {

    private final FoundPortalService foundPortalService;

    // initialDelay = 5000
    // cron = "0 0 3 * * *", zone = "Asia/Seoul"
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runFoundPortalApiScheduler() {
        try {
            System.out.println("포털기관 습득물 API 스케줄러 시작");
            foundPortalService.saveFoundPortalData();
            System.out.println("포털기관 습득물 API 스케줄러 종료");
        } catch (Exception e) {
            System.out.println("포털기관 습득물 API 스케줄러 실패");
            e.printStackTrace();
        }
    }

}
