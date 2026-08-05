package com.example.springai.service.document;

import com.example.springai.dto.VectorSearchResponse;
import com.example.springai.entity.AppUser;
import com.example.springai.security.CurrentUser;
import com.example.springai.service.QueryRewriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorSearchService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VectorSearchService.class
            );

    private final CurrentUser currentUser;
    private final QueryRewriteService queryRewriteService;
    private final MetadataService metadataService;
    private final VectorStoreService vectorStoreService;
    private final double similarityThreshold;

    public VectorSearchService(
            CurrentUser currentUser,
            QueryRewriteService queryRewriteService,
            MetadataService metadataService,
            VectorStoreService vectorStoreService,
            @Value("${app.rag.document.similarity-threshold:0.65}")
            double similarityThreshold
    ) {
        this.currentUser =
                currentUser;

        this.queryRewriteService =
                queryRewriteService;

        this.metadataService =
                metadataService;

        this.vectorStoreService =
                vectorStoreService;

        this.similarityThreshold =
                similarityThreshold;
    }

    public List<VectorSearchResponse> search(
            String query,
            Integer requestedTopK
    ) {
        int topK =
                normalizeTopK(
                        requestedTopK
                );

        List<Document> searchResults =
                searchDocuments(
                        query,
                        topK
                );

        return searchResults.stream()
                .map(document ->
                        new VectorSearchResponse(
                                document.getId(),
                                document.getText(),
                                document.getScore(),
                                metadataService
                                        .createPublicMetadata(
                                                document.getMetadata()
                                        )
                        )
                )
                .toList();
    }

    public List<Document> searchDocuments(
            String query
    ) {
        return searchDocuments(
                query,
                5
        );
    }

    private List<Document> searchDocuments(
            String query,
            int topK
    ) {
        if (query == null
                || query.isBlank()) {

            throw new IllegalArgumentException(
                    "검색 질문은 비어 있을 수 없습니다."
            );
        }

        AppUser user =
                currentUser.getCurrentUser();

        String filterExpression =
                metadataService
                        .createSecurityFilter(
                                user
                        );

        String normalizedQuery =
                query.trim();

        String rewrittenQuery =
                rewriteQuerySafely(
                        normalizedQuery
                );

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(
                                rewrittenQuery
                        )
                        .topK(
                                topK
                        )
                        .similarityThreshold(
                                similarityThreshold
                        )
                        .filterExpression(
                                filterExpression
                        )
                        .build();

        List<Document> searchResults =
                vectorStoreService.search(
                        searchRequest
                );

        log.info(
                "VectorStore 검색 완료. resultCount={}, topK={}, threshold={}",
                searchResults.size(),
                topK,
                similarityThreshold
        );

        for (Document document : searchResults) {
            log.debug(
                    "검색 문서 ID={}, score={}, documentId={}, chunkIndex={}",
                    document.getId(),
                    document.getScore(),
                    document.getMetadata().get(
                            "documentId"
                    ),
                    document.getMetadata().get(
                            "chunkIndex"
                    )
            );
        }

        return searchResults;
    }

    private String rewriteQuerySafely(
            String query
    ) {
        try {
            String rewrittenQuery =
                    queryRewriteService.rewrite(
                            query
                    );

            if (rewrittenQuery == null
                    || rewrittenQuery.isBlank()) {

                log.warn(
                        "질문 재작성 결과가 비어 있어 원본 질문을 사용합니다."
                );

                return query;
            }

            return rewrittenQuery.trim();

        } catch (RuntimeException exception) {
            log.warn(
                    "질문 재작성에 실패하여 원본 질문으로 검색합니다. error={}",
                    exception.getMessage()
            );

            return query;
        }
    }

    private int normalizeTopK(
            Integer requestedTopK
    ) {
        if (requestedTopK == null) {
            return 5;
        }

        if (requestedTopK < 1
                || requestedTopK > 20) {

            throw new IllegalArgumentException(
                    "topK는 1부터 20 사이여야 합니다."
            );
        }

        return requestedTopK;
    }
}