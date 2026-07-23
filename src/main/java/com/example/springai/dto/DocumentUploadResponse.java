/*
 * =============================================================================
 * 클래스명 : DocumentUploadResponse
 * =============================================================================
 * 목적
 *  - 업로드된 문서의 파일 정보를 반환한다.
 *  - 문서에서 추출한 텍스트와 추출 결과를 반환한다.
 */

package com.example.springai.dto;

public record DocumentUploadResponse(

        // 업로드된 원본 파일 이름
        String filename,

        // 업로드된 파일의 Content-Type
        String contentType,

        // 업로드된 원본 파일 크기
        long fileSize,

        // 생성된 Spring AI Document 개수
        int documentCount,

        // 추출된 전체 텍스트 길이
        int extractedTextLength,

        // 문서에서 추출한 전체 텍스트
        String extractedText
) {
}