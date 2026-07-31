/*
 * =============================================================================
 * 클래스명 : CustomAuthenticationSuccessHandler
 * =============================================================================
 * 목적
 *  - 사용자가 로그인에 성공하면 해당 사용자의 기존 대화를 삭제한다.
 *  - 대화 삭제 후 Spring AI 대화 화면으로 이동한다.
 */

package com.example.springai.security;

import com.example.springai.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final ConversationService conversationService;

    public CustomAuthenticationSuccessHandler(
            ConversationService conversationService
    ) {
        this.conversationService =
                conversationService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // 방금 로그인한 사용자의 기존 대화만 DB에서 삭제한다.
        conversationService.deleteMyConversations();

        // 대화 삭제가 완료되면 채팅 화면으로 이동한다.
        response.sendRedirect(
                "/chat"
        );
    }
}