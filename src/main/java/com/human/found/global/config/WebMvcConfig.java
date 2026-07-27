package com.human.found.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) 공지사항 가상 주소 연결
        registry.addResourceHandler("/images/notice/**")
                .addResourceLocations("file:/home/ubuntu/upload_images/notice/");

        // 2) 습득물 가상 주소 연결
        registry.addResourceHandler("/images/found/**")
                .addResourceLocations("file:/home/ubuntu/upload_images/found/");
        
        // 3) 분실물 가상 주소 연결
        registry.addResourceHandler("/images/lost/**")
                .addResourceLocations("file:/home/ubuntu/upload_images/lost/");  
                
        // 4) 채팅 가상 주소 연결
        registry.addResourceHandler("/images/chat/**")
                .addResourceLocations("file:/home/ubuntu/upload_images/chat/");
    }
}
