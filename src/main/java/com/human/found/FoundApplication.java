package com.human.found;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// ⚠️ 이제 완전히 하위 폴더 패키지가 되었으므로 부트 기본 자동 스캔 기능이 정상 작동합니다.
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class FoundApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoundApplication.class, args);
    }
}
