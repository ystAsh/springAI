/*
 * =============================================================================
 * 클래스명 : EmbeddingResponse
 * =============================================================================
 * 목적
 *  - 입력 문장의 Embedding 생성 결과를 반환한다.
 *  - 생성된 벡터의 차원과 일부 값을 확인할 수 있게 한다.
 */

package com.example.springai.dto;

import java.util.List;

public record EmbeddingResponse(

        // Embedding 변환에 사용한 원본 문장
        String text,

        // 생성된 전체 벡터의 숫자 개수
        int dimensions,

        // 테스트 화면에서 확인할 일부 벡터 값
        List<Double> preview
) {
}