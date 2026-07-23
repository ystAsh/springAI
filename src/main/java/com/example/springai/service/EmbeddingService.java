/*
 * =============================================================================
 * 클래스명 : EmbeddingService
 * =============================================================================
 * 목적
 *  - 사용자가 입력한 텍스트를 EmbeddingModel에 전달한다.
 *  - 텍스트를 숫자 벡터로 변환한다.
 *  - 벡터의 차원과 일부 값을 응답 DTO로 반환한다.
 */

package com.example.springai.service;

import com.example.springai.dto.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(
            EmbeddingModel embeddingModel
    ) {
        this.embeddingModel = embeddingModel;
    }

    /*
     * 입력받은 텍스트를 Embedding 벡터로 변환하고,
     * 벡터의 차원과 앞부분 일부 값을 반환한다.
     */
    public EmbeddingResponse embed(
            String text
    ) {
        // 입력 문장이 null이거나 비어 있는지 검사
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Embedding으로 변환할 문장을 입력해 주세요."
            );
        }

        // Google GenAI Embedding API를 호출하여 벡터 생성
        float[] vector =
                embeddingModel.embed(text);

        // 생성된 벡터가 비어 있는지 검사
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException(
                    "Embedding 벡터가 생성되지 않았습니다."
            );
        }

        // 전체 벡터 중 앞에서 최대 10개까지만 추출
        int previewSize =
                Math.min(vector.length, 10);

        // float 배열 앞부분을 Double 목록으로 변환
        List<Double> preview =
                Arrays.stream(
                                toDoubleArray(
                                        vector,
                                        previewSize
                                )
                        )
                        .boxed()
                        .toList();

        // Embedding 결과 DTO 반환
        return new EmbeddingResponse(
                text,
                vector.length,
                preview
        );
    }

    /*
     * float 배열의 앞부분을
     * double 배열로 변환한다.
     */
    private double[] toDoubleArray(
            float[] vector,
            int size
    ) {
        // 반환할 double 배열 생성
        double[] result =
                new double[size];

        // 필요한 개수만 float에서 double로 변환
        for (int index = 0;
             index < size;
             index++) {

            result[index] =
                    vector[index];
        }

        // 변환된 배열 반환
        return result;
    }
}