package com.human.found.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.human.found.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 로그 찍을 때 쓰는 롬북 어노테이션
@Slf4j 
@Component
@RequiredArgsConstructor
public class DormantScheduler {
    
    private final UserMapper userMapper;

    // initialDelay = 5000
    // cron = "0 * * * * *", zone = "Asia/Seoul"
    @Scheduled(cron = " 0 0 1 * * *", zone = "Asia/Seoul")
    public void updateDormantUsers(){
        int count = userMapper.updateDormant();

        if (count > 0) {
            log.info("휴면 계정 전환 완료: {}건", count);
        }
    }
}
