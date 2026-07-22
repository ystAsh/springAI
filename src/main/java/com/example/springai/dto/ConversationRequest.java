/*
 * =============================================================================
 * 클래스명 : ConversationRequest
 * =============================================================================
 * 목적
 *  - 사용자 질문을 전달하는 요청 DTO
 */

package com.example.springai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConversationRequest(

        @NotBlank(message = "질문을 입력하세요.")
        @Size(
                max = 2000,
                message = "질문은 2000자를 초과할 수 없습니다."
        )
        String question

) {
}