/*
 * =============================================================================
 * 클래스명 : VectorSearchResponse
 * =============================================================================
 * 목적
 *  - VectorStore 유사도 검색 결과를 반환한다.
 *  - 검색된 Chunk 내용, 유사도 점수와 문서 메타데이터를 제공한다.
 */

package com.example.springai.dto;

import java.util.Map;

public record VectorSearchResponse(

        // VectorStore 내부에서 사용하는 Chunk 고유 ID
        String id,

        // 검색된 문서 Chunk의 실제 텍스트
        String content,

        // 사용자 질문과 문서 Chunk 사이의 유사도 점수
        Double score,

        // 문서 권한 및 출처 메타데이터
        Map<String, Object> metadata
) {
}