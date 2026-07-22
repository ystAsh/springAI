/*
 * =============================================================================
 * 클래스명 : ConversationController
 * =============================================================================
 * 목적
 *  - 로그인 사용자의 대화 저장 및 조회 API를 제공하는 Controller
 */

package com.example.springai.controller;

import com.example.springai.dto.ConversationRequest;
import com.example.springai.dto.ConversationResponse;
import com.example.springai.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

    /* 새로운 대화를 저장한다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse save(
            @Valid
            @RequestBody ConversationRequest request) {

        return conversationService.save(request);
    }

    /* 현재 로그인 사용자의 대화 목록을 조회한다. */
    @GetMapping
    public List<ConversationResponse> findMyConversations() {

        return conversationService.findMyConversations();
    }
}