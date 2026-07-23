/*
 * =============================================================================
 * 클래스명 : GlobalExceptionHandler
 * =============================================================================
 * 목적
 *  - 애플리케이션에서 발생한 공통 예외를 처리한다.
 *  - 잘못된 요청에 대해 일정한 JSON 오류 응답을 반환한다.
 */

package com.example.springai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * 파일 검증이나 문서 처리 과정에서 발생한
     * IllegalArgumentException을 처리한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        // 오류 응답 데이터를 순서대로 저장
        Map<String, Object> errorResponse =
                new LinkedHashMap<>();

        // 오류가 발생한 시간 저장
        errorResponse.put(
                "timestamp",
                LocalDateTime.now()
        );

        // HTTP 상태 코드 저장
        errorResponse.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        // HTTP 오류 이름 저장
        errorResponse.put(
                "error",
                HttpStatus.BAD_REQUEST.getReasonPhrase()
        );

        // 실제 오류 메시지 저장
        errorResponse.put(
                "message",
                exception.getMessage()
        );

        // JSON 오류 응답 반환
        return errorResponse;
    }
}