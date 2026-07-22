/*
 * =============================================================================
 * 클래스명 : ConversationResponse
 * =============================================================================
 * 목적
 *  - 저장된 대화 정보를 반환하는 DTO
 */

package com.example.springai.dto;

import com.example.springai.entity.Conversation;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String username,
        String question,
        String answer,
        LocalDateTime createdAt
) {

    /* Conversation Entity를 응답 DTO로 변환한다. */
    public static ConversationResponse from(Conversation conversation) {

        return new ConversationResponse(
                conversation.getId(),
                conversation.getUsername(),
                conversation.getQuestion(),
                conversation.getAnswer(),
                conversation.getCreatedAt()
        );
    }
}