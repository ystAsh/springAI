/*
 * =============================================================================
 * 클래스명 : EmbeddingController
 * =============================================================================
 * 목적
 *  - Embedding 생성 테스트 API를 제공한다.
 *  - 사용자가 입력한 문장을 EmbeddingService에 전달한다.
 *  - 생성된 벡터의 차원과 일부 값을 반환한다.
 */

package com.example.springai.controller;

import com.example.springai.dto.EmbeddingResponse;
import com.example.springai.service.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(
            EmbeddingService embeddingService
    ) {
        this.embeddingService = embeddingService;
    }

    /*
     * 입력받은 문장을 Embedding 벡터로 변환하고,
     * 벡터 생성 결과를 반환한다.
     */
    @GetMapping
    public EmbeddingResponse embed(
            @RequestParam("text") String text
    ) {
        // 입력 문장을 EmbeddingService에 전달
        return embeddingService.embed(text);
    }
}