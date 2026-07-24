/*
 * =============================================================================
 * 클래스명 : ChatPageController
 * =============================================================================
 * 목적
 *  - 로그인 사용자가 이용할 브라우저 대화 화면을 반환한다.
 *  - /chat 요청을 Thymeleaf의 chat.html 화면과 연결한다.
 */

package com.example.springai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {

    // 브라우저 대화 화면을 반환한다.
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }
}