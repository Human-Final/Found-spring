package com.human.found.global.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/test-error")
public class TestErrorController {

    @GetMapping("/403")
    public String test403() {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "접근 권한이 없습니다."
        );
    }

    @GetMapping("/404")
    public String test404() {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "페이지를 찾을 수 없습니다."
        );
    }

    @GetMapping("/500")
    public String test500() {
        throw new RuntimeException("테스트용 500 에러");
    }

    @GetMapping("/400")
    public String test400() {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청입니다."
        );
    }
}