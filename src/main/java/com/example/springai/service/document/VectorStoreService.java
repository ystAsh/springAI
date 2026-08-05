package com.example.springai.service.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(
            VectorStore vectorStore
    ) {
        this.vectorStore =
                vectorStore;
    }

    /**
     * 문서 Chunk를 임베딩한 뒤 VectorStore에 저장한다.
     */
    public void addDocuments(
            List<Document> documents
    ) {
        if (documents == null
                || documents.isEmpty()) {

            throw new IllegalArgumentException(
                    "VectorStore에 저장할 문서가 없습니다."
            );
        }

        boolean containsInvalidDocument =
                documents.stream()
                        .anyMatch(document ->
                                document == null
                                        || document.getText() == null
                                        || document.getText().isBlank()
                        );

        if (containsInvalidDocument) {
            throw new IllegalArgumentException(
                    "비어 있는 문서 Chunk는 저장할 수 없습니다."
            );
        }

        vectorStore.add(
                documents
        );
    }

    /**
     * 구성된 검색 요청을 VectorStore에 전달한다.
     */
    public List<Document> search(
            SearchRequest searchRequest
    ) {
        if (searchRequest == null) {
            throw new IllegalArgumentException(
                    "VectorStore 검색 요청이 필요합니다."
            );
        }

        List<Document> searchResults =
                vectorStore.similaritySearch(
                        searchRequest
                );

        if (searchResults == null) {
            return List.of();
        }

        return List.copyOf(
                searchResults
        );
    }
}