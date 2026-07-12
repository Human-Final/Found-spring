package com.human.found.global.common.advice;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalApiExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleApiException(Exception e){
        log.error("API exception", e);

        ErrorResponse response = new ErrorResponse(500, "요청 처리 중 오류가 발생했습니다.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    // record : java에서 값을 담는 전용 클래스를 짧게 쓰는 문법
    // {} 안이 비어 있어도 괄호 안에 있는 status, message가 필드 역할을 함 
    //      -> api/ajax에러를 json으로 내려줌
    public record ErrorResponse(int status, String message) {
    }

}
