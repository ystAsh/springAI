/*
 * =============================================================================
 * 클래스명 : VectorStoreConfig
 * =============================================================================
 * 목적
 *  - Spring AI의 VectorStore 객체를 Spring Bean으로 등록한다.
 *  - 기존 Google Gemini EmbeddingModel을 이용해 SimpleVectorStore를 생성한다.
 *  - 9단계 문서 Chunk 저장 및 권한 기반 유사도 검색에 사용할 저장소를 제공한다.
 */

package com.example.springai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(
            EmbeddingModel embeddingModel
    ) {
        return SimpleVectorStore
                .builder(embeddingModel)
                .build();
    }
}