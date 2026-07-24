package com.human.found.infrastructure.policeAPI.foundAPI.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.human.found.infrastructure.policeAPI.foundAPI.service.FoundPortalService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
// @Profile("!replace-test")
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
