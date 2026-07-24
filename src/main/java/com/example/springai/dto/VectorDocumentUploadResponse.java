/*
 * =============================================================================
 * 클래스명 : VectorDocumentUploadResponse
 * =============================================================================
 * 목적
 *  - 문서를 VectorStore에 저장한 결과를 반환한다.
 *  - 저장된 문서 ID, 파일명, Chunk 개수와 권한 정보를 제공한다.
 */

package com.example.springai.dto;

public record VectorDocumentUploadResponse(

        // 업로드된 전체 문서를 구분하는 고유 ID
        String documentId,

        // 사용자가 업로드한 원본 파일명
        String fileName,

        // VectorStore에 저장된 전체 Chunk 개수
        int chunkCount,

        // 문서가 소속된 조직 ID
        String organizationId,

        // 문서가 소속된 부서 ID
        String departmentId,

        // 문서 접근에 필요한 보안등급
        int securityLevel,

        // 문서를 업로드한 사용자 ID
        Long ownerId,

        // 문서 저장 결과 메시지
        String message
) {
}