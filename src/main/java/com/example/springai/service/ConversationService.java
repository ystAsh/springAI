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

    /*
     * 현재 로그인 사용자의 질문을 Gemini에 전달하고,
     * 질문과 AI 답변을 DB에 저장한다.
     */
    @Transactional
    public ConversationResponse save(
            ConversationRequest request
    ) {
        // 현재 로그인 사용자 조회
        AppUser user = currentUser.getCurrentUser();

        // 요청 DTO에서 질문 추출
        String question = request.question();

        // Gemini에 질문을 전달하여 답변 생성
        String answer = aiChatService.generateAnswer(question);

        // 저장할 Conversation Entity 생성
        Conversation conversation = new Conversation(
                user.getId(),
                user.getUsername(),
                question,
                answer
        );

        // DB 저장
        Conversation savedConversation =
                conversationRepository.save(conversation);

        // Entity를 응답 DTO로 변환
        return ConversationResponse.from(savedConversation);
    }

    /*
     * 현재 로그인 사용자의 대화 목록을
     * 최신순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> findMyConversations() {
        // 현재 로그인 사용자 조회
        AppUser user = currentUser.getCurrentUser();

        // 로그인 사용자의 대화만 최신순으로 조회
        return conversationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }
}