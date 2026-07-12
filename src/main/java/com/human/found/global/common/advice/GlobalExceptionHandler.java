package com.human.found.global.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// 네이밍 규칙
//      핸들러 : 어떤 사건이나 요청을 직접 처리하는 클래스/메서드
//      어드바이스 : 여러 컨트롤러에 공통으로 적용되는 보조 규칙

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception e,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        int status;
        String message;

        /*
         * Spring이 HTTP 상태코드를 이미 가지고 던진 예외
         *
         * 예:
         * 400 잘못된 요청
         * 404 없는 주소
         * 405 지원하지 않는 요청 방식
         * 415 지원하지 않는 Content-Type
         * ResponseStatusException
         */
        if (e instanceof ErrorResponse errorResponse) {

            status = errorResponse.getStatusCode().value();
            message = resolveMessage(status, e);

            if (status >= 500) {
                log.error(
                    "HTTP server exception. method={}, url={}, status={}, exception={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    e.getClass().getSimpleName(),
                    e
                );
            } else {
                log.warn(
                    "HTTP request exception. method={}, url={}, status={}, exception={}, message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    e.getClass().getSimpleName(),
                    e.getMessage()
                );
            }

        } else {

            /*
             * HTTP 상태가 정해지지 않은 실제 내부 오류
             *
             * 예:
             * NullPointerException
             * MyBatis 오류
             * SQL 오류
             * Thymeleaf 파싱 오류
             */
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            message = "요청을 처리하는 중 문제가 발생했습니다.";

            log.error(
                "Unhandled server exception. method={}, url={}, exception={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
            );
        }

        response.setStatus(status);

        model.addAttribute("status", status);
        model.addAttribute("message", message);

        return resolveErrorView(status);
    }

    /**
     * 상태코드 기준으로 에러 페이지 결정
     */
    private String resolveErrorView(int status) {

        return switch (status) {
            case 403 -> "error/403";
            case 404 -> "error/404";
            default -> {
                if (status >= 500) {
                    yield "error/500";
                }

                // 400, 405, 406, 415 등
                yield "error/error";
            }
        };
    }

    /**
     * 상태코드 기준으로 사용자 메시지 결정
     */
    private String resolveMessage(int status, Exception e) {

        // 서비스에서 직접 지정한 메시지가 있으면 사용
        if (e instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null) {

            return responseStatusException.getReason();
        }

        return switch (status) {
            case 403 -> "해당 페이지에 접근할 권한이 없습니다.";
            case 404 -> "페이지를 찾을 수 없습니다.";
            default -> {
                if (status >= 500) {
                    yield "요청을 처리하는 중 문제가 발생했습니다.";
                }

                yield "요청을 처리할 수 없습니다.";
            }
        };
    }
}
