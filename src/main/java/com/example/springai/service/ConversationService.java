/*
 * =============================================================================
 * 클래스명 : ConversationService
 * =============================================================================
 * 목적
 *  - 로그인 사용자의 질문을 Gemini에 전달한다.
 *  - Gemini 답변과 사용자 질문을 DB에 저장한다.
 *  - 로그인 사용자의 대화 목록을 조회한다.
 *  - 로그인 사용자의 대화 내역을 삭제한다.
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
        this.conversationRepository =
                conversationRepository;

        this.currentUser =
                currentUser;

        this.aiChatService =
                aiChatService;
    }

    // 기존 REST 요청 DTO를 이용해 대화를 저장한다.
    @Transactional
    public ConversationResponse save(
            ConversationRequest request
    ) {

        // 요청 객체가 없으면 저장을 중단한다.
        if (request == null) {
            throw new IllegalArgumentException(
                    "대화 요청이 필요합니다."
            );
        }

        // 요청 DTO의 질문을 문자열 저장 메서드로 전달한다.
        return save(
                request.question()
        );
    }

    // 브라우저에서 전달받은 질문을 저장한다.
    @Transactional
    public ConversationResponse save(
            String question
    ) {

        // 현재 로그인 사용자를 조회한다.
        AppUser user =
                currentUser.getCurrentUser();

        // 질문의 공백과 길이를 검사한다.
        String normalizedQuestion =
                normalizeQuestion(
                        question
                );

        // 업로드 문서를 검색하고 Gemini 답변을 생성한다.
        String answer =
                aiChatService.generateAnswer(
                        normalizedQuestion
                );

        // 질문과 AI 답변을 저장할 Entity를 생성한다.
        Conversation conversation =
                new Conversation(
                        user.getId(),
                        user.getUsername(),
                        normalizedQuestion,
                        answer
                );

        // 생성한 대화를 DB에 저장한다.
        Conversation savedConversation =
                conversationRepository.save(
                        conversation
                );

        // 저장된 Entity를 응답 DTO로 변환한다.
        return ConversationResponse.from(
                savedConversation
        );
    }

    // 현재 로그인 사용자의 대화를 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public List<ConversationResponse> findMyConversations() {

        // 현재 로그인 사용자를 조회한다.
        AppUser user =
                currentUser.getCurrentUser();

        // 현재 로그인 사용자의 대화만 최신순으로 조회한다.
        return conversationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(
                        ConversationResponse::from
                )
                .toList();
    }

    // 현재 로그인 사용자의 대화를 모두 삭제한다.
    @Transactional
    public void deleteMyConversations() {

        // 현재 로그인 사용자를 조회한다.
        AppUser user =
                currentUser.getCurrentUser();

        // 현재 로그인 사용자 ID와 일치하는 대화만 삭제한다.
        conversationRepository.deleteByUserId(
                user.getId()
        );
    }

    // 질문의 공백과 길이를 검사한다.
    private String normalizeQuestion(
            String question
    ) {

        // 질문이 null이거나 공백뿐이면 차단한다.
        if (question == null
                || question.isBlank()) {

            throw new IllegalArgumentException(
                    "질문은 비어 있을 수 없습니다."
            );
        }

        // 질문 앞뒤의 불필요한 공백을 제거한다.
        String normalizedQuestion =
                question.trim();

        // 질문이 최대 길이를 넘으면 차단한다.
        if (normalizedQuestion.length() > 4000) {
            throw new IllegalArgumentException(
                    "질문은 4000자를 초과할 수 없습니다."
            );
        }

        return normalizedQuestion;
    }
}