package com.example.springai.service;

import com.example.springai.dto.VectorDocumentUploadResponse;
import com.example.springai.dto.VectorSearchResponse;
import com.example.springai.service.document.DocumentUploadService;
import com.example.springai.service.document.VectorSearchService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문서 업로드 및 검색 API의 Facade 서비스.
 *
 * 실제 파일 처리, Chunking, Metadata 생성, VectorStore 저장 및 검색은
 * 각 전문 서비스에 위임한다.
 */
@Service
public class VectorDocumentService {

    private final DocumentUploadService documentUploadService;
    private final VectorSearchService vectorSearchService;

    public VectorDocumentService(
            DocumentUploadService documentUploadService,
            VectorSearchService vectorSearchService
    ) {
        this.documentUploadService =
                documentUploadService;

        this.vectorSearchService =
                vectorSearchService;
    }

    public VectorDocumentUploadResponse upload(
            MultipartFile file,
            Integer requestedSecurityLevel
    ) {
        return documentUploadService.upload(
                file,
                requestedSecurityLevel
        );
    }

    public List<VectorSearchResponse> search(
            String query,
            Integer requestedTopK
    ) {
        return vectorSearchService.search(
                query,
                requestedTopK
        );
    }

    public List<Document> searchDocuments(
            String query
    ) {
        return vectorSearchService.searchDocuments(
                query
        );
    }
}