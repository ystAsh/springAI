/*
 * =============================================================================
 * 클래스명 : ConversationService
 * =============================================================================
 * 목적
 *  - 로그인 사용자의 질문을 Gemini에 전달한다.
 *  - Gemini 답변과 사용자 질문을 DB에 저장한다.
 *  - 로그인 사용자의 대화 목록을 조회한다.
 */

package com.example.springai.service;

import com.example.springai.dto.ConversationRequest;
import com.example.springai.dto.ConversationResponse;
import com.example.springai.entity.AppUser;
import com.example.springai.entity.Conversation;
import com.example.springai.repository.ConversationRepository;
import com.example.springai.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final CurrentUser currentUser;
    private final AiChatService aiChatService;

    public ConversationService(
            ConversationRepository conversationRepository,
            CurrentUser currentUser,
            AiChatService aiChatService
    ) {
        this.conversationRepository = conversationRepository;
        this.currentUser = currentUser;
        this.aiChatService = aiChatService;
    }

    // 기존 REST 요청 DTO를 이용해 대화를 저장한다.
    @Transactional
    public ConversationResponse save(
            ConversationRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "대화 요청이 필요합니다."
            );
        }

        return save(request.question());
    }

    // 브라우저에서 전달받은 질문을 저장한다.
    @Transactional
    public ConversationResponse save(
            String question
    ) {
        // 현재 로그인 사용자 조회
        AppUser user = currentUser.getCurrentUser();

        // 질문 유효성 검사
        String normalizedQuestion =
                normalizeQuestion(question);

        // Gemini 답변 생성
        String answer =
                aiChatService.generateAnswer(
                        normalizedQuestion
                );

        // 대화 Entity 생성
        Conversation conversation =
                new Conversation(
                        user.getId(),
                        user.getUsername(),
                        normalizedQuestion,
                        answer
                );

        // 대화 저장
        Conversation savedConversation =
                conversationRepository.save(
                        conversation
                );

        return ConversationResponse.from(
                savedConversation
        );
    }

    // 현재 로그인 사용자의 대화를 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public List<ConversationResponse> findMyConversations() {
        AppUser user =
                currentUser.getCurrentUser();

        return conversationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    // 질문의 공백과 길이를 검사한다.
    private String normalizeQuestion(
            String question
    ) {
        if (question == null
                || question.isBlank()) {

            throw new IllegalArgumentException(
                    "질문은 비어 있을 수 없습니다."
            );
        }

        String normalizedQuestion =
                question.trim();

        if (normalizedQuestion.length() > 4000) {
            throw new IllegalArgumentException(
                    "질문은 4000자를 초과할 수 없습니다."
            );
        }

        return normalizedQuestion;
    }
}