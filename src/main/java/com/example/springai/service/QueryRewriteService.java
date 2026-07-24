/*
 * =============================================================================
 * 클래스명 : QueryRewriteService
 * =============================================================================
 * 목적
 *  - 사용자의 질문을 문서 검색에 적합한 질문으로 변환한다.
 *  - 원본 질문과 재작성된 질문을 서버 로그에 출력한다.
 */

package com.example.springai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QueryRewriteService {

    // 현재 클래스의 서버 로그를 출력하는 객체
    private static final Logger log =
            LoggerFactory.getLogger(
                    QueryRewriteService.class
            );

    // 검색 질문을 재작성할 때 사용하는 Spring AI ChatClient
    private final ChatClient chatClient;

    public QueryRewriteService(
            ChatClient.Builder chatClientBuilder
    ) {
        this.chatClient =
                chatClientBuilder.build();
    }

    public String rewrite(
            String originalQuery
    ) {

        // 검색 질문이 비어 있는지 확인한다.
        if (originalQuery == null
                || originalQuery.isBlank()) {

            throw new IllegalArgumentException(
                    "검색 질문은 비어 있을 수 없습니다."
            );
        }

        // 서버 로그에 원본 질문을 출력한다.
        log.info(
                "원본 검색 질문: {}",
                originalQuery
        );

        // LLM이 원본 질문을 검색용 질문으로 재작성한다.
        String rewrittenQuery =
                chatClient.prompt()
                        .system("""
                                당신은 사내 문서 검색 질문을 재작성하는 도우미입니다.

                                다음 규칙을 지키세요.

                                1. 원래 질문의 의미를 바꾸지 마세요.
                                2. 질문에 생략된 목적어나 조건을 자연스럽게 보완하세요.
                                3. 동의어와 유사 표현을 자연스럽게 포함하세요.
                                4. 문서에서 검색하기 쉬운 구체적인 질문으로 작성하세요.
                                5. 질문에 대한 정답은 작성하지 마세요.
                                6. 설명이나 번호를 붙이지 마세요.
                                7. 검색용 질문 한 문장만 반환하세요.
                                """)
                        .user(originalQuery.trim())
                        .call()
                        .content();

        // LLM 응답이 비어 있으면 원본 질문을 사용한다.
        if (rewrittenQuery == null
                || rewrittenQuery.isBlank()) {

            log.warn(
                    "재작성 결과가 비어 있어 원본 질문을 사용합니다."
            );

            return originalQuery.trim();
        }

        // 서버 로그에 재작성된 질문을 출력한다.
        log.info(
                "재작성된 검색 질문: {}",
                rewrittenQuery.trim()
        );

        return rewrittenQuery.trim();
    }
}