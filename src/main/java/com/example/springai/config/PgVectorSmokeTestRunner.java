package com.example.springai.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PgVectorSmokeTestRunner implements ApplicationRunner {

    private final VectorStore vectorStore;

    public PgVectorSmokeTestRunner(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {

        Document testDocument = new Document(
                "우리 회사의 연차휴가는 입사 1년 이상 직원에게 연간 15일이 부여됩니다.",
                Map.of(
                        "documentId", "TEST-DOC-001",
                        "organizationId", "ORG-001",
                        "departmentId", "DEPT-001",
                        "securityLevel", 1,
                        "documentType", "POLICY"
                )
        );

        vectorStore.add(List.of(testDocument));

        System.out.println("========================================");
        System.out.println("PGVector 테스트 문서 저장 완료");
        System.out.println("========================================");

        SearchRequest searchRequest = SearchRequest.builder()
                .query("연차휴가는 며칠인가요?")
                .topK(3)
                .build();

        List<Document> searchResults =
                vectorStore.similaritySearch(searchRequest);

        System.out.println("PGVector 검색 결과 수: " + searchResults.size());

        for (Document result : searchResults) {
            System.out.println("----------------------------------------");
            System.out.println("문서 ID: " + result.getId());
            System.out.println("내용: " + result.getText());
            System.out.println("메타데이터: " + result.getMetadata());
        }

        System.out.println("========================================");
    }
}