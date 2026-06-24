package com.human.found.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // 브라우저가 /images/notice/파일명 으로 이미지를 달라고 요청하면
        // 강의실 공유 폴더 네트워크 주소(\\192.168.0.53\...\notice\) 내부를 뒤져서 파일을 배달해 줍니다.
        registry.addResourceHandler("/images/notice/**")
                .addResourceLocations("file:////192.168.0.53/260126/0608/배민선, 박상화, 김태연, 신민철/file/notice/");

        //습득물
        registry.addResourceHandler("/images/found/**")
                .addResourceLocations("file:////192.168.0.53/260126/0608/found/file/found/");
        
        //분실물
        registry.addResourceHandler("/images/lost/**")
                .addResourceLocations("file:////192.168.0.53/260126/0608/found/file/lost/");        
    }
}
