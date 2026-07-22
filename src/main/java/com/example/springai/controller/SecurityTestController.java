package com.example.springai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {
    @GetMapping("/")
    public Map<String, String> publicApi() {
        return Map.of(
                "message", "인증 없이 접근 가능한 API입니다."
        );
    }

    @GetMapping("/chat/test")
    public Map<String, String> protectedApi() {
        return Map.of(
                "message", "인증된 사용자만 접근 가능한 채팅 API입니다."
        );
    }
}
