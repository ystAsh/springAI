/*
 * =============================================================================
 * 클래스명 : ChatApiController
 * =============================================================================
 * 목적
 *  - 브라우저 대화창에서 전달한 질문을 처리한다.
 *  - Gemini 답변을 생성하고 저장된 대화 목록을 반환한다.
 *  - 현재 로그인 사용자의 대화 내역을 삭제한다.
 */

package com.example.springai.controller;

import com.example.springai.dto.ChatMessageRequest;
import com.example.springai.dto.ConversationResponse;
import com.example.springai.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ConversationService conversationService;

    public ChatApiController(
            ConversationService conversationService
    ) {
        this.conversationService =
                conversationService;
    }

    // 질문을 Gemini에 전달하고 결과를 저장한다.
    @PostMapping("/messages")
    public ResponseEntity<ConversationResponse> sendMessage(
            @RequestBody
            ChatMessageRequest request
    ) {

        ConversationResponse response =
                conversationService.save(
                        request.question()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 현재 로그인 사용자의 대화 목록을 반환한다.
    @GetMapping("/messages")
    public ResponseEntity<List<ConversationResponse>>
    getMessages() {

        return ResponseEntity.ok(
                conversationService
                        .findMyConversations()
        );
    }

    // 현재 로그인 사용자의 대화를 모두 삭제한다.
    @DeleteMapping("/messages")
    public ResponseEntity<Void> deleteMyConversations() {

        conversationService
                .deleteMyConversations();

        return ResponseEntity
                .noContent()
                .build();
    }
}