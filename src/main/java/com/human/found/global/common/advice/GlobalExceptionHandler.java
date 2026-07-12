package com.human.found.global.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

// 네이밍 규칙
//      핸들러 : 어떤 사건이나 요청을 직접 처리하는 클래스/메서드
//      어드바이스 : 여러 컨트롤러에 공통으로 적용되는 보조 규칙

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(
            ResponseStatusException e,
            HttpServletRequest request,
            Model model){
        
        int status = e.getStatusCode().value();

        log.warn("ResponseStatusException. url={}, status={}, reason={}",
                request.getRequestURI(),
                status,
                e.getReason()
        );

        model.addAttribute("status", status);
        model.addAttribute("message", e.getReason());

        if(status == HttpStatus.FORBIDDEN.value()){
            return "error/403";
        }

        if(status == HttpStatus.NOT_FOUND.value()){
            return "error/404";
        }

        return "error/error";
    }

    // 없는 정적 리소스, 없는 URL 접근 처리
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(
            NoResourceFoundException e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("No resource found. url={}", request.getRequestURI());

        model.addAttribute("status", 404);
        model.addAttribute("message", "페이지를 찾을 수 없습니다.");

        return "error/404";
    }

    // 매핑된 컨트롤러가 없는 경우 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("No handler found. url={}", request.getRequestURI());

        model.addAttribute("status", 404);
        model.addAttribute("message", "페이지를 찾을 수 없습니다.");

        return "error/404";
    }
    
    // 그 외 예상 못 한 서버 오류 처리
    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception e,
            HttpServletRequest request,
            Model model){
        
        log.error("Unhandled exception. url={}", request.getRequestURI(), e);

        model.addAttribute("status", 500);
        model.addAttribute("message",
                "요청을 처리하는 중 문제가 발생했습니다.");

        return "error/500";
    }
}
