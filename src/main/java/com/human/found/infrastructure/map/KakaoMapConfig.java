package com.human.found.infrastructure.map; // 본인의 패키지명으로 바꾸십시오.

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kakao.map")
@Getter 
@Setter
public class KakaoMapConfig {
    private String jsKey;
}