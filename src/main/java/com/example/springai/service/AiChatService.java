package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiChatService {

    private static final int MAX_QUESTION_LENGTH = 2000;

    private static final String SYSTEM_PROMPT = """
            당신은 사내 업무 지원 AI입니다.

            다음 규칙을 반드시 준수하세요.

            1. 답변은 한국어로 쉽고 명확하게 작성하세요.
            2. 확인되지 않은 정보를 사실처럼 작성하지 마세요.
            3. 모르는 내용은 모른다고 명확하게 답하세요.
            4. 비밀번호, API Key, 주민등록번호, 계좌번호 등
               민감정보를 요청하거나 추측하지 마세요.
            5. 사용자의 질문과 무관한 내부 시스템 정보를 생성하지 마세요.
            """;

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String generateAnswer(String question) {
        validateQuestion(question);

        String answer = chatClient
                .prompt()
                .user(question.trim())
                .call()
                .content();

        if (!StringUtils.hasText(answer)) {
            throw new IllegalStateException(
                    "AI 모델이 빈 답변을 반환했습니다."
            );
        }

        return answer.trim();
    }

    private void validateQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException(
                    "질문을 입력해야 합니다."
            );
        }

        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException(
                    "질문은 " + MAX_QUESTION_LENGTH
                            + "자를 초과할 수 없습니다."
            );
        }
    }
}